package com.backend.givr.organization.security;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.shared.email.EmailService;
import com.resend.core.exception.ResendException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceWorker {
    @Autowired
    private EmailService emailService;
    @Autowired
    private ProjectRepo repo;

    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void createProjectSegment(Project project){
        if(!project.getBroadcastEnabled())
            return;
        try{
            String segmentId = emailService.createProjectSegment(project.getTitle());
            project.setSegmentId(segmentId);
            repo.save(project);
        } catch (ResendException e) {
            System.err.printf("Failed to create segment for project %s", e.getLocalizedMessage());
        }
    }
}
