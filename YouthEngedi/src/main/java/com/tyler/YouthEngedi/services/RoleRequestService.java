package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.ResourceNotFoundException;
import com.tyler.YouthEngedi.Repository.RoleRequestRepository;
import com.tyler.YouthEngedi.Repository.UserRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.annotations.RateLimited;
import com.tyler.YouthEngedi.models.RoleRequest;
import com.tyler.YouthEngedi.models.User;
import com.tyler.YouthEngedi.models.dtos.RoleChangeRequest;
import com.tyler.YouthEngedi.models.enums.RequestStatus;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.models.mappers.RoleRequestMapper;
import jakarta.mail.MessagingException;
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
public class RoleRequestService {
    private final static Logger logger = LogManager.getLogger(RoleRequestService.class);
    @Autowired
    private RoleRequestRepository repository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleRequestMapper roleRequestMapper;

    @LogExecutionTime(value="Fetch all roleRequests in RoleRequestService class",doSave = false)
    public ResponseEntity<Page<RoleChangeRequest>> findAllRoleRequests(int page, int size) {

        Page<RoleRequest> roleRequests = repository.findAll(PageRequest.of(page,size));

        Page<RoleChangeRequest> roleChangeRequests = roleRequests.map(roleRequestMapper::mapToRoleChangeRequest);

        return ResponseEntity.ok(roleChangeRequests);
    }

    @LogExecutionTime(value="Sent a role request to developer in RoleRequestService class",doSave = false)
    public ResponseEntity<?> sendRoleRequest(long userId) {
        try{
            User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if(repository.existsByUserAndRequestStatus(user,RequestStatus.PENDING)){
                return new ResponseEntity<>("Request is still being processed",HttpStatus.CONFLICT);
            }
            requestUpgrade(user);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

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
        String body = String.format("""
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
    </head>
    <body style="margin:0;padding:20px;background:#f4f4f4;font-family:Arial,sans-serif;">

        <table width="100%%" cellpadding="0" cellspacing="0">
            <tr>
                <td align="center">

                    <table width="600" cellpadding="0" cellspacing="0"
                           style="background:#ffffff;border-radius:8px;overflow:hidden;
                                  box-shadow:0 2px 8px rgba(0,0,0,0.1);">

                        <tr>
                            <td style="background:#f59e0b;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">Role Request Submitted</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello,</p>

                                <p>
                                    Your role request has been submitted successfully.
                                </p>

                                <p>
                                    An administrator will review your application and notify you of
                                    their decision via email.
                                </p>

                                <p>
                                    No further action is required from you at this time.
                                </p>

                                <p>
                                    Thank you for your willingness to serve the Youth Engedi community.
                                </p>

                                <p>
                                    God bless,<br>
                                    <strong>Engedi Administration</strong>
                                </p>

                            </td>
                        </tr>

                        <tr>
                            <td style="padding:15px;background:#f3f4f6;
                                       text-align:center;font-size:12px;color:#6b7280;">
                                This is an automated message from the Youth Engedi Management System.
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """);

        try{
            emailService.sendEmail(user.getEmail(),subject,body);
        } catch (MessagingException e){
            // change null to user before going to production
            logger.error("Failed to send email to {} with subject {}",user.getEmail(),subject,e);
        }
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
