package com.backend.givr.organization.security;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.CloudinaryService;
import com.backend.givr.shared.service.RenderProjectService;
import com.cloudinary.Cloudinary;
import com.resend.core.exception.ResendException;
import jakarta.persistence.EntityManager;
import org.apache.hc.core5.concurrent.CompletedFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class ProjectServiceWorker {
    @Autowired
    private EmailService emailService;
    @Autowired
    private ProjectRepo repo;

    @Value("${givr.baseUrl}")
    private String apiBaseUrl;
    @Value("${api.version}")
    private String apiVersion;
    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private RenderProjectService renderProjectService;
    @Autowired
    private CloudinaryService cloudinaryService;

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

    /**
     * Dynamically creates a shareable project card*/
    @Async
    @Transactional
    public CompletableFuture<String> createProjectCard(Project project){

        try{
            var projectDto = mapper.toDto(project);
            byte[] imageByte = renderProjectService.renderProjectCard(projectDto);
            String securedUrl = cloudinaryService.uploadImage(imageByte, project.getProjectId());
            String shareableLink = String.format("%s/%s/share/project/%s", apiBaseUrl, apiVersion, project.getProjectId());

            saveUrl(project.getProjectId(), securedUrl, shareableLink);
            return CompletableFuture.completedFuture(shareableLink);
        } catch (RuntimeException e) {
            System.err.printf("Failed to create project card %s", e.getLocalizedMessage());
            throw new RuntimeException(e);
        }

    }

    @Transactional
    private void saveUrl(Long projectId, String secureUrl, String shareableLink){
        Project project = repo.findById(projectId).orElseThrow();
        project.setProjectCardUrl(secureUrl);
        project.setShareableLink(shareableLink);
    }
}
