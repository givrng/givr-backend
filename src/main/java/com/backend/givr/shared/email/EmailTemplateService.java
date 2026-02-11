package com.backend.givr.shared.email;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final SpringTemplateEngine engine;

    public String otpEmail(String otp, Duration duration){
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("appName", "Givr");
        context.setVariable("expiryMinutes", duration.toMinutes());
        return engine.process("email/otp", context);
    }

    public String volunteerWelcomeEmail(String firstname, String volunteerDashboardUrl){
        Context context = new Context();
        context.setVariable("firstname", firstname);
        context.setVariable("clientAppUrl", volunteerDashboardUrl);
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

    public String notificationForAuthUser() {
        Context context = new Context();
        context.setVariable("firstname", "User");
        return engine.process("email/oauthNotification", context);
    }
}
