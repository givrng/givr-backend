package com.backend.givr.shared.notification;

import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.enums.OtpPurpose;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.exceptions.FailedToSendOTPException;
import com.backend.givr.shared.otp.OTP;
import com.backend.givr.shared.otp.OTPGenerator;
import com.backend.givr.shared.otp.OTPService;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    @Value("${RESEND_API_TOKEN}")
    private String apiToken;

    @Autowired
    private OTPService otpService;

    @Autowired
    private EmailTemplateService emailTemplateService;
    private Resend resend;

    private final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @PostConstruct
    private void setResend(){
        this.resend = new Resend(apiToken);
    }

    public void sendOtpTo(String email, AccountType accountType, OtpPurpose purpose)  {
        String otpToken = OTPGenerator.generateOTP();
        String html = emailTemplateService.otpEmail(otpToken, OTPGenerator.DURATION);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Givr Notification <no-reply@notifications.givr.ng>")
                .to(email)
                .subject("GIVR OTP Request")
                .html(html)
                .build();
        OTP otp = otpService.generateOtp(email, otpToken, accountType, purpose);

        if(otp.isSent())
            return;

        try {
            CreateEmailResponse data = resend.emails().send(params);
            otpService.markAsSent(otp, data.getId());
        } catch (ResendException e) {
            otpService.deleteOtp(otp);
            throw new FailedToSendOTPException(e.getLocalizedMessage());
        }
    }

    public void sendPasswordChangeNotificationForOauthUser( String email){
        String html = emailTemplateService.notificationForAuthUser();
        sendEmail(html, email, "Password Reset");
    }

    public void sendWelcomeEmail(String firstname, String volunteerDashboardUrl, String email){
        String html = emailTemplateService.volunteerWelcomeEmail(firstname, volunteerDashboardUrl);
        sendEmail(html, email, "Welcome to Givr");
    }

    public void sendOrganizationWelcomeEmail(String firstname, String organizationDashboardUrl, String email){
        String html = emailTemplateService.organizationWelcomeEmail(firstname, organizationDashboardUrl);
        sendEmail(html, email, "Welcome to Givr");
    }

    public void sendApplicationSubmittedEmail(String firstname,String projectName, String organizationName, String address, String recipient){
        String html = emailTemplateService.applicationSubmittedEmail(firstname, projectName, organizationName, address);
        sendEmail(html, recipient, "Project application submitted");
    }

    public void sendApplicationApproved(String firstname,String projectName, String organizationName, String address, String recipient){
        String html = emailTemplateService.applicationApproved(firstname, projectName, organizationName, address);
        sendEmail(html, recipient, "Project application approved");
    }

    public void sendApplicationRejected(String firstname,String projectName, String organizationName, String recipient){
        String html = emailTemplateService.applicationRejected(firstname, projectName, organizationName);
        sendEmail(html, recipient, "Project application rejected");
    }

    private void sendEmail (String html, String recipient, String subject){
        CreateEmailOptions params = CreateEmailOptions.builder()
                .from("Givr Notification <no-reply@notifications.givr.ng>")
                .to(recipient)
                .subject(subject)
                .html(html)
                .build();
        try {
            CreateEmailResponse data = resend.emails().send(params);
        } catch (ResendException e) {
            logger.error("Error while sending email to {}, {}", recipient, e.getLocalizedMessage());
        }
    }

    public void sendVerificationStatusUpdate(@NotBlank String contactFirstname, String email, ReviewStatus reviewStatus, String reason) {
        String html = emailTemplateService.verificationUpdate(contactFirstname, reviewStatus, reason);
        sendEmail(html, email, "Account verification update");
    }

    public void sendParticipationUpdate(String firstname, String projectName, String email, String organizationName, ParticipationStatus status){
        String html = switch (status){
            case COMPLETED -> emailTemplateService.projectCompleted(firstname, projectName, organizationName);
            case REJECTED -> emailTemplateService.participationRejected(firstname, projectName, organizationName);
            case null, default -> null;
        };
        String subject = status== ParticipationStatus.COMPLETED? "Congratulations on completing a project": "Project participation Update";
        sendEmail(html, email, subject );
    }
}
