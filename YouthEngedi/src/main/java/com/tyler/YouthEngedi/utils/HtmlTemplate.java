package com.tyler.YouthEngedi.utils;

import java.time.LocalDate;

import static com.tyler.YouthEngedi.constants.UrlConstants.*;

public final class HtmlTemplate {


    public static String removeEventHtml(String title, LocalDate eventDate, String startTime, String endTime){
        return String.format("""
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
                title,
                eventDate,
                startTime,
                endTime
        );
    }

    public static String createEventHtml(String title, LocalDate eventDate, String startTime, String endTime){
        return String.format("""
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
                title,
                eventDate,
                startTime,
                endTime,
                production ? FRONTEND_CALENDER_PROD : FRONTEND_CALENDER_DEV
        );
    }

    public static String verificationEmailHtml(String link){
        return String.format("""
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
    }

    public static String contactSubmissionHtml(String name,String email,String message){
        return String.format("""
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
                name,
                email,
                message
        );
    }

    public static String emailHtml(String homeUrl, String resetUrl) {
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

        return htmlTemplate.replace("{{home_url}}", homeUrl).replace("{{reset_url}}", resetUrl);
    }

    public static String basicEmailRequest(String name, String email, String message) {
        return String.format("""
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
                name,
                email,
                message
        );
    }
}
