package com.backend.givr.shared.email;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.enums.*;
import com.backend.givr.shared.exceptions.FailedToSendOTPException;
import com.backend.givr.shared.otp.OTP;
import com.backend.givr.shared.otp.OTPGenerator;
import com.backend.givr.shared.otp.OTPService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.broadcasts.model.CreateBroadcastOptions;
import com.resend.services.broadcasts.model.CreateBroadcastResponseSuccess;
import com.resend.services.broadcasts.model.SendBroadcastOptions;
import com.resend.services.contacts.model.*;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.resend.services.segments.model.CreateSegmentOptions;
import com.resend.services.segments.model.CreateSegmentResponseSuccess;
import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class EmailService {

    @Value("${RESEND_API_TOKEN}")
    private String apiToken;

    @Value("${givr.allowed.admins}")
    private List<String> admins;

    @Autowired
    private OTPService otpService;

    @Autowired
    private ThymeleafTemplateService emailTemplateService;
    @Autowired
    private ObjectMapper mapper;

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

    public void sendApplicationNotificationEmail(String organizationName,String projectName, String recipient){
        String html = emailTemplateService.applicationNotificationEmail(organizationName, projectName);
        sendEmail(html, recipient, "Project application notification");
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

    @Async
    public void sendVerificationStatusUpdate(@NotBlank String contactFirstname, String email, ReviewStatus reviewStatus, String reason) {
        String html = emailTemplateService.verificationUpdate(contactFirstname, reviewStatus, reason);
        sendEmail(html, email, "Account verification update");
    }

    @Transactional
    @Async
    public void sendParticipationUpdate(Volunteer volunteer, Project project, ParticipationStatus status){
        String html = switch (status){
            case COMPLETED -> {
                updateGivrAdmin(project, volunteer);
                yield emailTemplateService.projectCompleted(volunteer.getFirstname(), project.getTitle(), project.getOrganization().getOrganizationName());
            }
            case REJECTED -> emailTemplateService.participationRejected(volunteer.getFirstname(), project.getTitle(), project.getOrganization().getOrganizationName());
            case null, default -> null;
        };
        String subject = status== ParticipationStatus.COMPLETED? "Congratulations on completing a project": "Project participation Update";
        sendEmail(html, volunteer.getEmail(), subject );
    }

    private void updateGivrAdmin(Project project, Volunteer volunteer){
        String fullName = String.format("%S, %s", volunteer.getLastname(), volunteer.getFirstname());
        String html = emailTemplateService.projectCompleteAdminUpdate(volunteer.getEmail(), project.getTitle(), fullName);

        String subject = "Volunteer project completion notification";
        admins.forEach(admin-> sendEmail(html, admin, subject));
    }
    /**
     * Creates a conversation segment for a project
     * Allowing an organization broadcast to users within the segment
     * @return segment ID */
    public String createProjectSegment(String projectName) throws ResendException {
        CreateSegmentOptions options = CreateSegmentOptions.builder()
                .name(projectName)
                .build();
        CreateSegmentResponseSuccess response = resend.segments().create(options);
        return response.getId();
    }

    /**
     * Creates a recipient for organization broadcasts
     * @return Contact ID*/

    public String createContact(String email, String firstname, String lastname) throws ResendException {
        System.out.printf("%s %s %s", firstname, lastname, email);
        CreateContactOptions params = CreateContactOptions.builder()
                .email(email)
                .firstName(firstname)
                .lastName(lastname)
                .unsubscribed(false)
                .build();

        CreateContactResponseSuccess data = resend.contacts().create(params);
        return  data.getId();
    }

    @Async
    public  void addContactToSegment(String segmentId, String contactId) throws ResendException {
        AddContactToSegmentOptions options = AddContactToSegmentOptions.builder()
                .segmentId(segmentId)
                .id(contactId)
                .build();
        resend.contacts().segments().add(options);
    }

    @Async
    public void broadcastToParticipants(String message, String segmentId, String organizationName) throws ResendException {
        CreateBroadcastOptions options = CreateBroadcastOptions.builder()
                .segmentId(segmentId)
                .name(organizationName)
                .from("Givr Notification <no-reply@notifications.givr.ng>")
                .subject(String.format("%s notification", organizationName))
                .text(message)
                .build();

        CreateBroadcastResponseSuccess data = resend.broadcasts().create(options);

        SendBroadcastOptions params = SendBroadcastOptions.builder()
                .scheduledAt("in 1 min")
                .build();
        resend.broadcasts().send(params, data.getId());
    }

    public void removeParticipantFromSegment(String email, String segmentId) throws ResendException {
        RemoveContactFromSegmentOptions options = RemoveContactFromSegmentOptions.builder()
                .email(email)
                .segmentId(segmentId)
                .build();

        resend.contacts().segments().remove(options);
    }

    public void deleteSegment(String segmentId) throws ResendException {
       resend.segments().remove(segmentId);
    }
}
