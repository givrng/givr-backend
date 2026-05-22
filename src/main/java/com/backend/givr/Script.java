package com.backend.givr;

import com.backend.givr.organization.service.ParticipationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Script implements Runnable{
    @Autowired
    private ParticipationService participationService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private EmailService emailService;

    @Override
    public void run() {

    }

    public void start(){
        run();
    }
}
