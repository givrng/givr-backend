package com.backend.givr.shared.email;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Duration;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ThymeleafTemplateService {
    private final SpringTemplateEngine engine;
    @Value("${client.app.baseUrl}")
    private String clientBaseUrl;

    public String otpEmail(String otp, Duration duration){
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("appName", "Givr");
        context.setVariable("expiryMinutes", duration.toMinutes());
        return engine.process("email/otp", context);
    }

    public String volunteerWelcomeEmail(String firstname, String volunteerDashboardUrl){
        Context context = new Context();
        String whatsappLink = "https://chat.whatsapp.com/GpYcrwx1qtt13jyOKVJUvq?mode=gi_t";
        context.setVariable("firstname", firstname);
        context.setVariable("clientAppUrl", volunteerDashboardUrl);
        context.setVariable("whatsappLink", whatsappLink);
        return engine.process("email/volunteerWelcome", context);
    }

    public String organizationWelcomeEmail(String firstname, String organizationDashboard){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("clientAppUrl", organizationDashboard);
        return engine.process("email/organizationWelcome", context);
    }


    public String applicationSubmittedEmail(String firstname,String projectName, String organizationName, String address){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("projectName", projectName);
        context.setVariable("organizationName", organizationName);
        context.setVariable("address", address);
        return engine.process("email/applicationSubmitted", context);
    }

    public String applicationNotificationEmail(String organizationName, String projectName){
        Context context = new Context();
        context.setVariable("organizationName", organizationName);
        context.setVariable("projectName", projectName);
        return engine.process("email/applicationNotification", context);
    }

    public String applicationApproved(String firstname,String projectName, String organizationName, String address){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("projectName", projectName);
        context.setVariable("organizationName", organizationName);
        context.setVariable("address", address);
        return engine.process("email/applicationApproved", context);
    }

    public String applicationRejected(String firstname,String projectName, String organizationName){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("projectName", projectName);
        context.setVariable("organizationName", organizationName);
        return engine.process("email/applicationRejected", context);
    }

    public String projectCompleted(String firstname, String projectName,String organizationName){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("projectName", projectName);
        context.setVariable("organizationName", organizationName);
        return engine.process("email/projectCompletion", context);
    }

    public String certificateReady(String firstname, String projectName,String organizationName){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("projectName", projectName);
        context.setVariable("organizationName", organizationName);
        return engine.process("email/certificateReadyNotification", context);
    }
    public String projectCompleteAdminUpdate(String email, String projectName, String fullName){
        Context context = new Context();
        context.setVariable("volunteerFullName", fullName);
        context.setVariable("volunteerEmail", email);
        context.setVariable("projectName", projectName);
        context.setVariable("completionDate", LocalDate.now());

        return engine.process("email/adminUpdate", context);
    }

    public String participationRejected(String firstname, String projectName,String organizationName){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("projectName", projectName);
        context.setVariable("organizationName", organizationName);
        return engine.process("email/participationRejected", context);
    }
    public String notificationForAuthUser() {
        Context context = new Context();
        context.setVariable("firstname", "User");
        return engine.process("email/oauthNotification", context);
    }

    public String verificationUpdate(@NotBlank String contactFirstname, ReviewStatus reviewStatus, String reason) {
        Context context = new Context();
        context.setVariable("firstname", contactFirstname);
        context.setVariable("status", reviewStatus);
        context.setVariable("reason", reason);
        return engine.process("email/verificationUpdate", context);
    }

    public String projectCard(String title, String description, String cardUrl, String clientUrl, String projectUrl){
        Context context = new Context();
        context.setVariable("projectTitle", title);
        context.setVariable("projectDescription", description);
        context.setVariable("projectCardUrl", cardUrl);
        context.setVariable("clientUrl", clientUrl);
        context.setVariable("projectUrl", projectUrl);
        return engine.process("email/projectCard", context);
    }

    public String projectListCTA(Project p, String volunteerFirstname){

        Context context = new Context();
        String description = p.getDescription();
        int shortDescriptionLength = description.length() < 150 ? description.length(): Math.toIntExact(Math.round(description.length() * 0.7));

        context.setVariable("projectCardUrl", p.getProjectCardUrl());
        context.setVariable("firstname", volunteerFirstname);
        context.setVariable("projectName", p.getTitle());
        context.setVariable("organizationName", p.getOrganization().getOrganizationName());
        context.setVariable("address", p.getAddress());
        context.setVariable("startDate", p.getStartDate().toString());
        context.setVariable("endDate", p.getEndDate().toString());
        context.setVariable("description", String.format("%s ...", p.getDescription().substring(shortDescriptionLength)));
        context.setVariable("shareableLink", p.getShareableLink());

        return engine.process("email/ProjectListingCTA", context);
    }

    public String chatNotification(String volunteerName, String organizationName, String projectTitle, String content) {
        Context context = new Context();
        context.setVariable("volunteerName", volunteerName);
        context.setVariable("organizationName", organizationName);
        context.setVariable("projectTitle", projectTitle);
        context.setVariable("content", content);
        context.setVariable("chatUrl", String.format("%s/volunteer", clientBaseUrl));
        return engine.process("email/chatNotification", context);
    }

    public String joinWhatsAppNotification(String firstName, String whatsappLink, Integer memberCount){
        Context context = new Context();

        context.setVariable("firstName", firstName);
        context.setVariable("whatsappLink", whatsappLink);
        context.setVariable("memberCount", memberCount);

        return engine.process("email/JoinWhatsApp",context);
    }
    public String sendNotification(String firstName){
        Context context = new Context();
        context.setVariable("firstName", firstName);
        return engine.process("email/notification",context);
    }
}

