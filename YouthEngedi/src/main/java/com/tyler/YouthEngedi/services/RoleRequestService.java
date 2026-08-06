package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Exceptions.RoleRequestPendingException;
import com.tyler.YouthEngedi.Repository.RoleRequestRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.models.RoleRequest;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.RoleChangeRequest;
import com.tyler.YouthEngedi.models.enums.RequestStatus;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.models.mappers.RoleRequestMapper;
import com.tyler.YouthEngedi.utils.HtmlTemplate;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoleRequestService {
    private final static Logger logger = LogManager.getLogger(RoleRequestService.class);
    private final RoleRequestRepository repository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RoleRequestMapper roleRequestMapper;

    @LogExecutionTime(value="Fetch all roleRequests in RoleRequestService class",doSave = false)
    public Page<RoleChangeRequest> findAllRoleRequests(int page, int size) {

        Page<RoleRequest> roleRequests = repository.findAll(PageRequest.of(page,size));

        return roleRequests.map(roleRequestMapper::mapToRoleChangeRequest);
    }

    @LogExecutionTime(value="Sent a role request to developer in RoleRequestService class",doSave = false)
    public void sendRoleRequest(long userId) {
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if(repository.existsByUserAndRequestStatus(user,RequestStatus.PENDING)){
                throw new RoleRequestPendingException("Request is still being processed");
            }
            requestUpgrade(user);
    }

    @LogExecutionTime(value="Building role request and updating user status in RoleUpgradeService class",doSave = false)
    public void requestUpgrade(User user){

        Role role = userService.getNextRole(user.getRoles());

        String userReason = String.format("The user is currently assigned the roles %s and has requested a role upgrade to %s. This change would grant additional privileges aligned with their intended responsibilities and participation requirements.",user.getRoles(),role);
        RoleRequest request = RoleRequest
                .builder()
                .requestStatus(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .requestedRole(role)
                .userReason(userReason)
                .wasReviewed(false)
                .user(user)
                .build();

        String subject = "Role Request Submitted Successfully";
        String body = HtmlTemplate.roleRequestUpgrade();

        emailService.sendEmail(user.getEmail(),subject,body);

        emailService.sendAdminRequest(request);

        repository.save(request);
    }

    private void acceptRoleUpgrade(RoleChangeRequest request){
        RoleRequest roleRequest = repository.findById(request.getRoleReqId()).orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        User reviewBy = userService.getCurrentAdmin();

        roleRequest.setRequestStatus(RequestStatus.APPROVED);
        roleRequest.setReviewAt(LocalDateTime.now());
        roleRequest.setReviewedBy(reviewBy);

        User user = roleRequest.getUser();

        user.getRoles().add(roleRequest.getRequestedRole());

        user.setRoles(user.getRoles());

        userRepository.save(user);

        repository.save(roleRequest);

        emailService.sendApprovedRequest(reviewBy,user,roleRequest);

        removeRoleRequest(request.getRoleReqId());
    }

    private void rejectRoleUpgrade(RoleChangeRequest request){
        RoleRequest roleRequest = repository.findById(request.getRoleReqId()).orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        User reviewBy = userService.getCurrentAdmin();

        roleRequest.setRequestStatus(RequestStatus.REJECTED);
        roleRequest.setReviewAt(LocalDateTime.now());
        roleRequest.setReviewedBy(reviewBy);

        User user = roleRequest.getUser();

        repository.save(roleRequest);

        emailService.sendRejectedRequest(reviewBy,user,roleRequest);

        removeRoleRequest(request.getRoleReqId());
    }

    private void removeRoleRequest(long id){

        RoleRequest request = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        request.setWasReviewed(true);

        repository.save(request);
    }

    @LogExecutionTime(value="Building role request and updating user status in RoleUpgradeService class",doSave = false)
    public void updateRequest(RoleChangeRequest request){

        try{
            RequestStatus status = RequestStatus.valueOf(request.getRequestStatus().toUpperCase());

            switch(status){
                case RequestStatus.REJECTED:
                    rejectRoleUpgrade(request);
                    break;
                case RequestStatus.APPROVED:
                    acceptRoleUpgrade(request);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid options");

            }
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }
    }

}
