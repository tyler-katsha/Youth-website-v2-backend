package com.tyler.YouthEngedi.services;

import com.tyler.YouthEngedi.Exceptions.InvalidEmailException;
import com.tyler.YouthEngedi.Repository.ContactSubmissionRepository;
import com.tyler.YouthEngedi.annotations.AuditAction;
import com.tyler.YouthEngedi.annotations.LogExecutionTime;
import com.tyler.YouthEngedi.models.*;
import com.tyler.YouthEngedi.models.dtos.EmailRequest;
import com.tyler.YouthEngedi.models.enums.AuthProvider;
import com.tyler.YouthEngedi.models.enums.EventType;
import com.tyler.YouthEngedi.models.enums.RequestStatus;
import com.tyler.YouthEngedi.models.enums.Role;
import com.tyler.YouthEngedi.utils.HtmlTemplate;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Record;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;
import static com.tyler.YouthEngedi.services.CookieService.production;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final ContactSubmissionRepository contactSubmissionRepository;
    private final JavaMailSender mailSender;
    private final RestClient restClient;

    @Value("${spring.mail.username}")
    private String developerEmail;
    @Value("${spring.mail.youth.email}")
    private String adminEmail;
    @Value("${resend.api.key}")
    private String resendKey;

    private final static String noReplyEmail = "noreply@youthengedi.co.za";
    private final static String supportEmail = "support@youthengedi.co.za";

    public void sendEmail(EmailRequest request) {
        final Map<String,Object> body = new HashMap<>();

            String subject = String.format("New Contact Message from %s", request.getName());

            String emailBody = HtmlTemplate.basicEmailRequest(request.getName(),request.getEmail(),request.getMessage());

            body.put("from",request.getEmail());
            body.put("to", List.of(request.getEmail()));
            body.put("subject",subject);
            body.put("body",emailBody);


            ContactSubmission contactSubmission = ContactSubmission.builder()
                    .senderEmail(request.getEmail())
                    .senderName(request.getName())
                    .message(request.getMessage())
                    .submittedAt(LocalDateTime.now())
                    .build();

            contactSubmissionRepository.save(contactSubmission);

        callRenderAPI(body);
    }

    @Async
    public void sendEmail(PasswordResetRequest request) throws MessagingException {

        final Map<String,Object> body = new HashMap<>();

        String subject = "Password Reset Request - Youth Management System";

        String homeUrl = production ? FRONTEND_URL_PROD : FRONTEND_URL_DEV;
        String resetUrl = String.format(production ? FRONTEND_RESET_PASSWORD_PROD : FRONTEND_RESET_PASSWORD_DEV, request.getToken());

        String finalHtmlBody = HtmlTemplate.emailHtml(homeUrl,resetUrl);


        body.put("from",request.getEmail());
        body.put("to",List.of(noReplyEmail));
        body.put("subject",subject);
        body.put("body",finalHtmlBody);

        callRenderAPI(body);
    }

    public void sendEmail(String emailBody,String subject) {

        final Map<String,Object> body = new HashMap<>();

        body.put("from",developerEmail);
        body.put("to", List.of(developerEmail));
        body.put("subject",subject);
        body.put("body",emailBody);

        callRenderAPI(body);
    }

    public void sendEmail(String email,String subject, String msg) {

        final Map<String,Object> body = new HashMap<>();

        body.put("from",email);
        body.put("to", List.of(adminEmail));
        body.put("subject",subject);
        body.put("replyTo",email);
        body.put("body",msg);

        ContactSubmission contactSubmission = ContactSubmission.builder()
                    .subject(subject)
                    .senderEmail(email)
                    .message("A role request upgrade")
                    .submittedAt(LocalDateTime.now())
                    .build();

        contactSubmissionRepository.save(contactSubmission);

        callRenderAPI(body);

    }

    public void sendEmail(String aEmail, String email,String subject, String msg) {

        final Map<String,Object> body = new HashMap<>();

        body.put("from",email);
        body.put("to", List.of(aEmail));
        body.put("subject",subject);
        body.put("replyTo",email);
        body.put("body",msg);

        ContactSubmission contactSubmission = ContactSubmission.builder()
                    .subject(subject)
                    .senderEmail(email)
                    .message("A role request upgrade")
                    .submittedAt(LocalDateTime.now())
                    .build();

        contactSubmissionRepository.save(contactSubmission);

        callRenderAPI(body);
    }

    public void sendApprovedRequest(User admin, User user,RoleRequest request) {

        String subject = "Role Request Approved";

        String message = HtmlTemplate.roleRequestApprovedRequest(user.getName(),admin.getName(),admin.getEmail(),request.getRequestedRole(),request.getAdminComment());

        CompletableFuture.runAsync(() -> {
            sendEmail(admin.getEmail(),user.getEmail(), subject, message);
        });
    }

    public void sendRejectedRequest(User admin, User user, RoleRequest request) {
        String subject = "Role Request Rejected";

        String message = HtmlTemplate.roleRequestRejectedRequest(user.getName(),admin.getName(),admin.getEmail(),request.getRequestedRole(),request.getAdminComment());

        CompletableFuture.runAsync(() -> {
            sendEmail(user.getEmail(), subject, message);
        });
    }

    @Async
    public void sendAdminRequest(RoleRequest request) {

        User user = request.getUser();

        String subject = "New Role Upgrade Request";

        String message = HtmlTemplate.adminRequest(user.getName(),user.getEmail(),user.getRoles(),request.getRequestedRole(),request.getUserReason(),request.getRequestedAt(),request.getRequestStatus());

        CompletableFuture.runAsync(() -> {
            sendEmail(adminEmail,subject,message);
        });

    }

    @Async
    public void sendVerificationEmail(String email, String token) {

        final Map<String,Object> body = new HashMap<>();

        String link = String.format(production ? FRONTEND_VERIFICATION_PROD : FRONTEND_VERIFICATION_DEV,token,email);

        String subject = "Verify Your Youth Engedi Account";
        String bodyHtml = HtmlTemplate.verificationEmailHtml(link);

        body.put("from",email);
        body.put("to",noReplyEmail);
        body.put("subject",subject);
        body.put("body",bodyHtml);

        callRenderAPI(body);
    }

    public void sendTestEmail(String type) {

        String testToken = "token123";

        User admin = User.builder()
                .id(-999L)
                .name("Admin")
                .email(adminEmail)
                .authProvider(AuthProvider.LOCAL)
                .roles(Set.of(Role.MEMBER,Role.YOUTH_LEADER,Role.ADMIN))
                .enabled(true)
                .createdAt(LocalDateTime.MAX)
                .build();

        User u = User.builder()
                .id(-999L)
                .name("Bob").email(adminEmail)
                .authProvider(AuthProvider.OAUTH2)
                .roles(Set.of(Role.MEMBER))
                .enabled(true)
                .createdAt(LocalDateTime.MAX)
                .build();

        RoleRequest request = RoleRequest.builder()
                .roleReqId(-999L)
                .requestedRole(Role.ADMIN)
                .requestStatus(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.MAX)
                .reviewAt(LocalDateTime.MIN)
                .adminComment("Hello,\n\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\n\nNo action is required. If you received this email, the email service is functioning correctly.\n\nKind regards,\nYouth Engedi Management System")
                .userReason("Hello,\n\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\n\nNo action is required. If you received this email, the email service is functioning correctly.\n\nKind regards,\nYouth Engedi Management System")
                .wasReviewed(false)
                .user(null)
                .reviewedBy(null).build();

        Event event = Event.builder()
                .eventId(-999L)
                .title("Title 001")
                .startTime("00:00").endTime("23:59")
                .description("Hello,\n\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\n\nNo action is required. If you received this email, the email service is functioning correctly.\n\nKind regards,\nYouth Engedi Management System")
                .eventType(EventType.GENERAL)
                .createdByUserId(-99919L)
                .eventDate(LocalDate.EPOCH)
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .name("System")
                .email(adminEmail).subject("System Test")
                .message("Hello,\\n\\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\\n\\nNo action is required. If you received this email, the email service is functioning correctly.\\n\\nKind regards,\\nYouth Engedi Management System")
                .build();
            switch(type){
                case "verify" -> sendVerificationEmail(adminEmail,testToken);
                case "role-submitted" -> testRequestUpgrade(u);
                case "role-request" -> testRoleRequest(request);
                case "role-approved" -> sendApprovedRequest(admin,u,request);
                case "role-rejected" -> sendRejectedRequest(admin,u,request);
                case "event-created" -> testAddEvent(event);
                case "event-cancelled" -> testRemoveEvent(event);
                case "contact" -> testContact(emailRequest);
                default -> throw new IllegalArgumentException("Unknown email type.");

            }

    }

    private void testRequestUpgrade(User user){
        Role role = Role.ADMIN;

        String userReason = String.format("The user is currently assigned the roles %s and has requested a role upgrade to %s. This change would grant additional privileges aligned with their intended responsibilities and participation requirements.",user.getRoles(),role);

        RoleRequest request = RoleRequest
                .builder()
                .requestStatus(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.MAX)
                .requestedRole(role)
                .userReason(userReason)
                .wasReviewed(false)
                .user(user)
                .build();

        String subject = "Role Request Submitted Successfully";
        String body = HtmlTemplate.roleRequestUpgrade();


        CompletableFuture.runAsync(() -> {
            sendEmail(user.getEmail(),subject,body);
        });

        sendAdminRequest(request);
    }
    private void testRoleRequest(RoleRequest request){
        String subject = "Role Request Submitted Successfully";
        String body = HtmlTemplate.roleRequestUpgrade();

        CompletableFuture.runAsync(() -> {
            sendEmail(adminEmail,subject,body);
        });


        sendAdminRequest(request);
    }
    private void testAddEvent(Event event) {
        String subject = String.format("Event Added: %s", event.getTitle());
        String emailBody = HtmlTemplate.createEventHtml(event.getTitle(),event.getEventDate(),event.getStartTime(),event.getEndTime());

        CompletableFuture.runAsync(() -> {
            sendEmail(emailBody,subject);
        });
    }
    private void testRemoveEvent(Event event){
        String subject = String.format("Event Cancelled: %s",event.getTitle());
        String emailBody = HtmlTemplate.removeEventHtml(event.getTitle(),event.getEventDate(),event.getStartTime(),event.getEndTime());

        CompletableFuture.runAsync(() -> {
            sendEmail(emailBody,subject);
        });
    }
    private void testContact(EmailRequest request){

        final Map<String,Object> body = new HashMap<>();

        String subject = String.format("New Contact Message from %s", request.getName());

        String emailBody = HtmlTemplate.contactSubmissionHtml(request.getName(),request.getEmail(),request.getMessage());

        body.put("to",developerEmail);
        body.put("from",adminEmail);
        body.put("replyTo",request.getEmail());
        body.put("subject",subject);
        body.put("body",emailBody);

        callRenderAPI(body);
    }

    public boolean hasMXRecord(String email){
        try{
            String domain = email.substring(email.indexOf("@") + 1);

            Lookup lookup = new Lookup(domain, Type.MX);
            Record[] records = lookup.run();

            return records.length > 1;
        } catch (InvalidEmailException | TextParseException e){
            return false;
        }
    }

    private void callRenderAPI(Map<String,Object> body){
        restClient.post()
                .uri("/emails")
                .header("Authorization","Bearer " + resendKey)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}
