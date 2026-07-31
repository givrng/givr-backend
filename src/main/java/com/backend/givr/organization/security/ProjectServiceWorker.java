package com.backend.givr.organization.security;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.service.CloudinaryService;
import com.backend.givr.shared.service.GivrImageRendererService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import com.resend.core.exception.ResendException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ProjectServiceWorker {
    @Autowired
    private EmailService emailService;
    @Autowired
    private ProjectRepo repo;
    @Autowired
    private VolunteerRepo volunteerService;
    @Value("${givr.baseUrl}")
    private String apiBaseUrl;
    @Value("${api.version}")
    private String apiVersion;
    @Autowired
    private ProjectMapper mapper;
    @Autowired
    private GivrImageRendererService renderProjectService;
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
        if (project.getProjectFlierUrl() == null || project.getProjectFlierUrl().isEmpty()) {
            String shareableLink = String.format("%s/%s/share/project/%s", apiBaseUrl, apiVersion, project.getProjectId());
            saveUrl(project.getProjectId(), null, shareableLink);
            return CompletableFuture.completedFuture(null);
        }
        try{
            var projectDto = mapper.toDto(project);
            byte[] imageByte = renderProjectService.renderProjectCard(projectDto);
            String securedUrl = cloudinaryService.uploadImage(imageByte, "projects",project.getProjectId());
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

    public Mono<Void> sendProjectListing(Project project) {
        List<Volunteer> volunteers =
                volunteerService.findAllByLocationState(project.getLocation().getState());

        return Flux.fromIterable(volunteers)
                .delayElements(Duration.ofMillis(200))           // rate-limit dispatch
                .concatMap(volunteer ->                          // sequential, no overload
                        Mono.fromCallable(() -> {
                                    emailService.sendProjectListingNotification(
                                            project,
                                            volunteer.getFirstname(),
                                            volunteer.getEmail()
                                    );
                                    return volunteer;
                                })
                                .subscribeOn(Schedulers.boundedElastic())    // offload blocking I/O
                                .doOnSuccess(v ->
                                        log.info("Email sent to {}", v.getEmail()))
                                .onErrorResume(ex -> {
                                    log.error("Failed to notify volunteer {}: {}",
                                            volunteer.getEmail(), ex.getMessage());
                                    return Mono.empty();                     // skip and continue
                                })
                )
                .then();
    }
}
