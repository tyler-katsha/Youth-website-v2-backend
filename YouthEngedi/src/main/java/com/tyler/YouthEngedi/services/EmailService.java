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
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;
import org.xbill.DNS.Record;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;

@Service
public class EmailService {

    private static final Logger logger = LogManager.getLogger(EmailService.class);
    @Autowired
    private ContactSubmissionRepository contactSubmissionRepository;
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String developerEmail;
    @Value("${spring.mail.youth.email}")
    private String adminEmail;

    public void sendEmail(EmailRequest request) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        String subject = String.format("New Contact Message from %s", request.getName());

        String emailBody = String.format("""
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
                            <td style="background:#2563eb;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">New Contact Message</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello Administrator,</p>

                                <p>
                                    A new message has been submitted through the Youth Engedi contact form.
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Name</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Email</strong></td>
                                        <td>%s</td>
                                    </tr>

                                </table>

                                <h3 style="margin-top:30px;color:#111827;">Message</h3>

                                <div style="padding:15px;border:1px solid #e5e7eb;
                                            background:#ffffff;border-radius:6px;
                                            white-space:pre-wrap;">
                                    %s
                                </div>

                                <p style="margin-top:30px;">
                                    Please respond to the sender at your earliest convenience.
                                </p>

                                <p>
                                    Kind regards,<br>
                                    <strong>Youth Engedi System</strong>
                                </p>

                            </td>
                        </tr>

                        <tr>
                            <td style="padding:15px;background:#f3f4f6;
                                       text-align:center;font-size:12px;color:#6b7280;">
                                This is an automated notification from the Youth Engedi Management System.
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """,
                request.getName(),
                request.getEmail(),
                request.getMessage()
        );

        helper.setTo(request.getEmail());

        helper.setFrom(adminEmail);

        helper.setReplyTo(request.getEmail());

        helper.setSubject(subject);


        helper.setText(emailBody,true);

        ContactSubmission contactSubmission = ContactSubmission.builder().senderEmail(request.getEmail()).senderName(request.getName()).message(request.getMessage()).submittedAt(LocalDateTime.now()).build();

        contactSubmissionRepository.save(contactSubmission);

        mailSender.send(message);
    }

    @Async
    public void sendEmail(PasswordResetRequest request) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        String subject = "Password Reset Request - Youth Management System";
        String senderEmail = "no-reply@youthmanagementsystem.com";

        String homeUrl = FRONTEND_URL_PROD;
        String resetUrl = String.format(FRONTEND_RESET_PASSWORD_PROD, request.getToken());

        String htmlTemplate = """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
              <title>Reset Your Password</title>
              <style>
                /* Base Resets */
                body {
                  margin: 0;
                  padding: 0;
                  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                  background-color: #f4f4f7;
                  color: #51545e;
                  -webkit-text-size-adjust: 100%;
                  -ms-text-size-adjust: 100%;
                }
                table {
                  border-spacing: 0;
                  border-collapse: collapse;
                }
                td {
                  word-break: break-word;
                }
            
                /* Layout */
                .email-wrapper {
                  width: 100%;
                  margin: 0;
                  padding: 0;
                  background-color: #f4f4f7;
                }
                .email-content {
                  width: 100%;
                  margin: 0;
                  padding: 0;
                }
            
                /* Header */
                .email-masthead {
                  padding: 30px 0;
                  text-align: center;
                }
                .email-masthead_name {
                  font-size: 20px;
                  font-weight: bold;
                  color: #333333;
                  text-decoration: none;
                }
            
                /* Body */
                .email-body {
                  width: 100%;
                  margin: 0;
                  padding: 0;
                  border-top: 1px solid #e1e1e4;
                  border-bottom: 1px solid #e1e1e4;
                  background-color: #ffffff;
                }
                .email-body_inner {
                  width: 570px;
                  margin: 0 auto;
                  padding: 45px;
                }
            
                /* Typography */
                h1 {
                  margin-top: 0;
                  color: #333333;
                  font-size: 24px;
                  font-weight: bold;
                  text-align: left;
                }
                p {
                  margin-top: 0;
                  color: #51545e;
                  font-size: 16px;
                  line-height: 1.5em;
                  text-align: left;
                }
                a {
                  color: #3869d4;
                }
                .sub-text {
                  font-size: 13px;
                  color: #6b6e76;
                  margin-top: 20px;
                  padding-top: 20px;
                  border-top: 1px solid #e1e1e4;
                }
                .automated-text {
                  font-size: 14px;
                  font-style: italic;
                  color: #888888;
                  margin-top: 25px;
                }
            
                /* Button */
                .button-wrapper {
                  text-align: left;
                  margin: 30px 0;
                }
                .button {
                  display: inline-block;
                  padding: 12px 24px;
                  color: #ffffff !important;
                  background-color: #3869d4;
                  border-radius: 4px;
                  text-decoration: none;
                  font-weight: bold;
                  font-size: 16px;
                }
            
                /* Footer */
                .footer {
                  margin: 0 auto;
                  padding: 30px 0;
                  text-align: center;
                  width: 570px;
                }
                .footer p {
                  color: #a8aaaf;
                  font-size: 12px;
                  text-align: center;
                }
            
                /* Mobile Media Queries */
                @media only screen and (max-width: 600px) {
                  .email-body_inner,
                  .footer {
                    width: 100% !important;
                    padding-left: 20px !important;
                    padding-right: 20px !important;
                  }
                  .button-wrapper {
                    text-align: center !important;
                  }
                  .button {
                    display: block !important;
                    width: 100% !important;
                    box-sizing: border-box;
                  }
                }
              </style>
            </head>
            <body>
              <table class="email-wrapper" role="presentation">
                <tr>
                  <td align="center">
                    <table class="email-content" role="presentation">
            
                      <!-- Header -->
                      <tr>
                        <td class="email-masthead">
                          <a href="{{home_url}}" class="email-masthead_name">
                            Youth Management System
                          </a>
                        </td>
                      </tr>
            
                      <!-- Main Body -->
                      <tr>
                        <td class="email-body">
                          <table class="email-body_inner" align="center" role="presentation">
                            <tr>
                              <td>
                                <h1>Reset your password</h1>
                                <p>You recently requested to reset your password for your Youth Management System account. Click the button below to reset it. <strong>This link will expire in 24 hours.</strong></p>
            
                                <table width="100%" role="presentation">
                                  <tr>
                                    <td class="button-wrapper">
                                      <a href="{{reset_url}}" class="button" target="_blank">Reset Password</a>
                                    </td>
                                  </tr>
                                </table>
            
                                <p>If you did not request a password reset, please ignore this email. Your password will remain unchanged.</p>
                                
                                <p class="automated-text">This is an automated message by the Youth Management System. Please do not reply directly to this email.</p>
            
                                <div class="sub-text">
                                  <p>If you're having trouble clicking the "Reset Password" button, copy and paste the URL below into your web browser:</p>
                                  <p><a href="{{reset_url}}">{{reset_url}}</a></p>
                                </div>
            
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
            
                      <tr>
                        <td>
                          <table class="footer" align="center" role="presentation">
                            <tr>
                              <td align="center">
                                <p>&copy; 2026 Youth Management System. All rights reserved.</p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
            
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """;

        String finalHtmlBody = htmlTemplate.replace("{{home_url}}", homeUrl).replace("{{reset_url}}", resetUrl);

        helper.setTo(request.getEmail());
        helper.setFrom(senderEmail);
        helper.setSubject(subject);
        helper.setText(finalHtmlBody, true);

        mailSender.send(mimeMessage);
    }

    public void sendEmail(String emailBody,String subject) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setTo(developerEmail);

        helper.setFrom(adminEmail);

        helper.setSubject(subject);

        helper.setText(emailBody,true);

        mailSender.send(message);
    }

    public void sendEmail(String email,String subject, String msg) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setTo(email);

        helper.setFrom(adminEmail);

        helper.setReplyTo(email);

        helper.setSubject(subject);

        helper.setText(msg,true);

        ContactSubmission contactSubmission = ContactSubmission.builder().subject(subject).senderEmail(email).message("A role request upgrade").submittedAt(LocalDateTime.now()).build();

        contactSubmissionRepository.save(contactSubmission);

        mailSender.send(message);
    }

    public void sendEmail(String aEmail, String email,String subject, String msg) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        helper.setTo(aEmail);

        helper.setFrom(email);

        helper.setReplyTo(email);

        helper.setSubject(subject);

        helper.setText(msg,true);

        ContactSubmission contactSubmission = ContactSubmission.builder().subject(subject).senderEmail(email).message("A role request upgrade").submittedAt(LocalDateTime.now()).build();

        contactSubmissionRepository.save(contactSubmission);

        mailSender.send(message);
    }

    public void sendApprovedRequest(User admin, User user,RoleRequest request) {

        String subject = "Role Request Approved";

        String message = String.format("""
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
                            <td style="background:#16a34a;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">Role Request Approved</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Dear <strong>%s</strong>,</p>

                                <p>
                                    Congratulations! Your request to be assigned the role of
                                    <strong>%s</strong> has been reviewed and approved.
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Requested Role</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Status</strong></td>
                                        <td style="color:#16a34a;"><strong>APPROVED</strong></td>
                                    </tr>

                                    <tr>
                                        <td><strong>Reviewed By</strong></td>
                                        <td>%s (%s)</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Reason</strong></td>
                                        <td>%s</td>
                                    </tr>

                                </table>

                                <p style="margin-top:25px;">
                                    Your account permissions have now been updated and are active.
                                    If you are currently logged in, you may need to log out and log
                                    back in to refresh your permissions.
                                </p>

                                <p>
                                    If you have any questions regarding this decision, please contact
                                    the Engedi Administration team using the contact form on our website.
                                </p>

                                <p>
                                    Congratulations once again, and thank you for serving the ministry!
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
    """,
                user.getName(),
                request.getRequestedRole(),
                request.getRequestedRole(),
                admin.getName(),
                admin.getEmail(),
                request.getAdminComment() == null || request.getAdminComment().isBlank() ? "No reason provided." : request.getAdminComment());
        try{
            sendEmail(admin.getEmail(),user.getEmail(), subject, message);
        } catch (MessagingException e){
            logger.error("Failed to send email to {} with subject {}",admin.getEmail(),subject,e);
        }
    }

    public void sendRejectedRequest(User admin, User user, RoleRequest request) {
        String subject = "Role Request Rejected";

        String message = String.format("""
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
                            <td style="background:#dc2626;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">Role Request Rejected</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Dear <strong>%s</strong>,</p>

                                <p>
                                    Thank you for submitting your role request.
                                </p>

                                <p>
                                    After careful review, your request for the role of
                                    <strong>%s</strong> has not been approved at this time.
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Requested Role</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Status</strong></td>
                                        <td style="color:#dc2626;"><strong>REJECTED</strong></td>
                                    </tr>

                                    <tr>
                                        <td><strong>Reviewed By</strong></td>
                                        <td>%s (%s)</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Reason</strong></td>
                                        <td>%s</td>
                                    </tr>

                                </table>

                                <p style="margin-top:25px;">
                                    This decision does not prevent you from submitting another
                                    request in the future if your circumstances change.
                                </p>

                                <p>
                                    If you believe this decision was made in error or you require
                                    further clarification, please contact the Engedi Administration
                                    team using the contact form on our website.
                                </p>

                                <p>
                                    Thank you for your understanding and for your continued
                                    involvement in the Engedi community.
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
    """,
                user.getName(),
                request.getRequestedRole(),
                request.getRequestedRole(),
                admin.getName(),
                admin.getEmail(),
                request.getAdminComment() == null || request.getAdminComment().isBlank()
                        ? "No reason provided."
                        : request.getAdminComment()
        );
        try{
            sendEmail(user.getEmail(), subject, message);
        } catch (MessagingException e){
            logger.error("Failed to send email to {} with subject {}",user.getEmail(),subject,e);
        }
    }

    @Async
    public void sendAdminRequest(RoleRequest request) {

        User user = request.getUser();

        String subject = "New Role Upgrade Request";

        String message = String.format("""
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
                            <td style="background:#2563eb;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">New Role Request</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello Administrator,</p>

                                <p>
                                    A new role upgrade request has been submitted and is awaiting your review.
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Applicant Name</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Applicant Email</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Current Roles</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Requested Role</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Reason</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Submitted At</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Status</strong></td>
                                        <td style="color:#d97706;"><strong>%s</strong></td>
                                    </tr>

                                </table>

                                <p style="margin-top:25px;">
                                    Please review this request through the Youth Engedi Administration Panel
                                    and either approve or reject it.
                                </p>

                                <p>
                                    Thank you for helping keep the Youth Engedi community well managed.
                                </p>

                                <p>
                                    Kind regards,<br>
                                    <strong>Youth Engedi System</strong>
                                </p>

                            </td>
                        </tr>

                        <tr>
                            <td style="padding:15px;background:#f3f4f6;
                                       text-align:center;font-size:12px;color:#6b7280;">
                                This is an automated notification from the Youth Engedi Management System.
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """,
                user.getName(),
                user.getEmail(),
                user.getRoles(),
                request.getRequestedRole(),
                request.getUserReason() == null || request.getUserReason().isBlank()
                        ? "No reason provided."
                        : request.getUserReason(),
                request.getRequestedAt(),
                request.getRequestStatus()
        );
        try{
            sendEmail(adminEmail,subject,message);
        } catch (MessagingException e){
            logger.error("Failed to send email to {} with subject {}",adminEmail,subject,e);
        }
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

    @Async
    public void sendVerificationEmail(String email, String token) throws MessagingException {

        String link = String.format(FRONTEND_VERIFICATION_PROD,token,email);

        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

        String subject = "Verify Your Youth Engedi Account";
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
                            <td style="background:#2563eb;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">Verify Your Email Address</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello,</p>

                                <p>
                                    Welcome to <strong>Youth Engedi</strong>! Thank you for creating an account.
                                </p>

                                <p>
                                    Before you can start using your account, please verify your email address by clicking the button below.
                                </p>

                                <p style="text-align:center;margin:30px 0;">
                                    <a href="%s"
                                       style="display:inline-block;padding:12px 24px;
                                              background:#2563eb;color:#ffffff;
                                              text-decoration:none;border-radius:5px;font-weight:bold;">
                                        Verify My Account
                                    </a>
                                </p>
                                <br/>

                                <p>
                                    If the button above doesn't work, copy and paste the following link into your browser:
                                </p>

                                <p style="word-break:break-all;">
                                    <a href="%s">%s</a>
                                </p>

                                <p>
                                    If you did not create a Youth Engedi account, you can safely ignore this email.
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
    """,
                link,
                link,
                link
        );
        helper.setTo(email);
        helper.setSubject(subject);
        helper.setText(body,true);

        mailSender.send(message);
    }

    public void sendTestEmail(String type) throws MessagingException {

        String testToken = "token123";
        User admin = User.builder().id(-999L).name("Admin").email(adminEmail).authProvider(AuthProvider.LOCAL).roles(Set.of(Role.MEMBER,Role.YOUTH_LEADER,Role.ADMIN)).enabled(true).createdAt(LocalDateTime.MAX).build();
        User u = User.builder().id(-999L).name("Bob").email(adminEmail).authProvider(AuthProvider.OAUTH2).roles(Set.of(Role.MEMBER)).enabled(true).createdAt(LocalDateTime.MAX).build();
        RoleRequest request = RoleRequest.builder().roleReqId(-999L).requestedRole(Role.ADMIN).requestStatus(RequestStatus.PENDING).requestedAt(LocalDateTime.MAX).reviewAt(LocalDateTime.MIN).adminComment("Hello,\n\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\n\nNo action is required. If you received this email, the email service is functioning correctly.\n\nKind regards,\nYouth Engedi Management System").userReason("Hello,\n\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\n\nNo action is required. If you received this email, the email service is functioning correctly.\n\nKind regards,\nYouth Engedi Management System").wasReviewed(false).user(null).reviewedBy(null).build();
        Event event = Event.builder().eventId(-999L).title("Title 001").startTime("00:00").endTime("23:59").description("Hello,\n\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\n\nNo action is required. If you received this email, the email service is functioning correctly.\n\nKind regards,\nYouth Engedi Management System").eventType(EventType.GENERAL).createdByUserId(-99919L).eventDate(LocalDate.EPOCH).build();

        switch(type){
            case "verify" -> sendVerificationEmail(adminEmail,testToken);
            case "role-submitted" -> testRequestUpgrade(u);
            case "role-request" -> testRoleRequest(request);
            case "role-approved" -> sendApprovedRequest(admin,u,request);
            case "role-rejected" -> sendRejectedRequest(admin,u,request);
            case "event-created" -> testAddEvent(event);
            case "event-cancelled" -> testRemoveEvent(event);
            case "contact" -> testContact(EmailRequest.builder().name("System").email(adminEmail).subject("System Test").message("Hello,\\n\\nThis is a test message generated by the Youth Engedi Management System to verify email formatting, styling, and delivery.\\n\\nNo action is required. If you received this email, the email service is functioning correctly.\\n\\nKind regards,\\nYouth Engedi Management System").build());
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
            sendEmail(user.getEmail(),subject,body);
        } catch (MessagingException e){
            logger.error("Failed to send email to {} with subject {}",user.getEmail(),subject,e);
        }
        sendAdminRequest(request);
    }
    private void testRoleRequest(RoleRequest request){
        String subject = "Role Request Submitted Successfully";
        String body = """
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
    """;

        try{
            sendEmail(adminEmail,subject,body);
        } catch (MessagingException e){
            e.printStackTrace();
            //logger.error("Failed to send email to {} with subject {}",adminEmail,subject,e);
        }
        sendAdminRequest(request);
    }
    private void testAddEvent(Event event) {
        String emailBody = String.format("""
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
                            <td style="background:#16a34a;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">New Event Added</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello,</p>

                                <p>
                                    A new Youth Engedi event has been added to the calendar. We would love for you to join us!
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Event</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Date</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Time</strong></td>
                                        <td>%s - %s</td>
                                    </tr>

                                </table>

                                <p style="margin-top:25px;">
                                    We look forward to seeing you there. Don't forget to invite your friends and be part of what God is doing through our youth ministry!
                                </p>

                                <p>
                                    Click the button below to view the full event details in Youth Engedi.
                                </p>

                                <p>
                                    <a href="%s"
                                       style="display:inline-block;padding:12px 20px;
                                              background:#2563eb;color:white;
                                              text-decoration:none;border-radius:5px;">
                                        View Event
                                    </a>
                                </p>

                                <p>
                                    See you soon!
                                </p>

                                <p>
                                    God bless,<br>
                                    <strong>Engedi Youth Ministry</strong>
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
    """,
                event.getTitle(),
                event.getEventDate(),
                event.getStartTime(),
                event.getEndTime(),
                "http://localhost:5173/calendar"
        );

        String subject = String.format("Event Added: %s", event.getTitle());

        try{
            sendEmail(emailBody,subject);
        } catch (MessagingException e){
            // change null to user before going to production
            logger.error("Failed to send email to {} with subject {}","Everyone",subject,e);
        }
    }
    private void testRemoveEvent(Event event){
        String emailBody = String.format("""
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
        </head>
        <body style="margin:0;padding:0;background:#f4f4f4;font-family:Arial,sans-serif;">
            <table width="100%%" cellpadding="0" cellspacing="0" style="padding:30px 0;">
                <tr>
                    <td align="center">
                        <table width="600" cellpadding="0" cellspacing="0"
                               style="background:#ffffff;border-radius:10px;overflow:hidden;
                                      box-shadow:0 2px 8px rgba(0,0,0,0.1);">

                            <tr>
                                <td style="background:#dc2626;padding:25px;text-align:center;color:white;">
                                    <h1 style="margin:0;">Event Cancelled</h1>
                                </td>
                            </tr>

                            <tr>
                                <td style="padding:30px;color:#333333;line-height:1.6;">

                                    <p>Hello,</p>

                                    <p>
                                        We regret to inform you that the following event has been cancelled:
                                    </p>

                                    <table width="100%%" cellpadding="10" cellspacing="0"
                                           style="background:#f9fafb;border:1px solid #e5e7eb;
                                                  border-radius:8px;margin:20px 0;">
                                        <tr>
                                            <td><strong>Event</strong></td>
                                            <td>%s</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Date</strong></td>
                                            <td>%s</td>
                                        </tr>
                                        <tr>
                                            <td><strong>Time</strong></td>
                                            <td>%s - %s</td>
                                        </tr>
                                    </table>

                                    <p>
                                        We sincerely apologize for any inconvenience this may cause.
                                        Thank you for your understanding, and we hope to see you at
                                        our future events.
                                    </p>

                                    <p>
                                        If you have any questions, please feel free to contact the
                                        church leadership.
                                    </p>

                                    <p>
                                        God bless,<br>
                                        <strong>Engedi Youth Ministry</strong>
                                    </p>

                                </td>
                            </tr>

                            <tr>
                                <td style="background:#f3f4f6;padding:15px;text-align:center;
                                           font-size:12px;color:#6b7280;">
                                    This is an automated message from the Engedi Youth Management System.
                                </td>
                            </tr>

                        </table>
                    </td>
                </tr>
            </table>
        </body>
        </html>
        """,
                event.getTitle(),
                event.getEventDate(),
                event.getStartTime(),
                event.getEndTime()
        );

        String subject = String.format("Event Cancelled: %s",event.getTitle());

        try{
            sendEmail(emailBody,subject);
        } catch (MessagingException e){
            e.printStackTrace();
            //logger.error("Failed to send email to {} with subject {}","Everyone",subject,e);
        }
    }
    private void testContact(EmailRequest request){
        try{
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message,true,"UTF-8");

            String subject = String.format("New Contact Message from %s", request.getName());

            String emailBody = String.format("""
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
                            <td style="background:#2563eb;padding:25px;text-align:center;color:#ffffff;">
                                <h1 style="margin:0;">📨 New Contact Message</h1>
                            </td>
                        </tr>

                        <tr>
                            <td style="padding:30px;color:#333333;line-height:1.6;">

                                <p>Hello Administrator,</p>

                                <p>
                                    A new message has been submitted through the Youth Engedi contact form.
                                </p>

                                <table width="100%%" cellpadding="10" cellspacing="0"
                                       style="border:1px solid #e5e7eb;background:#f9fafb;border-radius:6px;">

                                    <tr>
                                        <td><strong>Name</strong></td>
                                        <td>%s</td>
                                    </tr>

                                    <tr>
                                        <td><strong>Email</strong></td>
                                        <td>%s</td>
                                    </tr>

                                </table>

                                <h3 style="margin-top:30px;color:#111827;">Message</h3>

                                <div style="padding:15px;border:1px solid #e5e7eb;
                                            background:#ffffff;border-radius:6px;
                                            white-space:pre-wrap;">
                                    %s
                                </div>

                                <p style="margin-top:30px;">
                                    Please respond to the sender at your earliest convenience.
                                </p>

                                <p>
                                    Kind regards,<br>
                                    <strong>Youth Engedi System</strong>
                                </p>

                            </td>
                        </tr>

                        <tr>
                            <td style="padding:15px;background:#f3f4f6;
                                       text-align:center;font-size:12px;color:#6b7280;">
                                This is an automated notification from the Youth Engedi Management System.
                            </td>
                        </tr>

                    </table>

                </td>
            </tr>
        </table>

    </body>
    </html>
    """,
                    request.getName(),
                    request.getEmail(),
                    request.getMessage()
            );

            helper.setTo(developerEmail);

            helper.setFrom(adminEmail);

            helper.setReplyTo(request.getEmail());

            helper.setSubject(subject);


            helper.setText(emailBody,true);


            mailSender.send(message);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
