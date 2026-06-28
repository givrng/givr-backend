package com.backend.givr;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ProjectRepo;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class Script {
    @Autowired
    private VolunteerRepo volunteerRepo;
    @Autowired
    private ProjectRepo projectRepo;
    @Autowired
    private RedisService redisService;

    @Autowired
    private EmailService emailService;
    @Value("${app.action}")
    private String action;

    public void sendNotificationToVolunteers(){
        System.out.println("Executing task");
        List<Volunteer> volunteers = volunteerRepo.findAll();

        Flux<Volunteer> volunteerFlux = Flux.fromIterable(volunteers);

        int memberCount = volunteers.size();

        volunteerFlux.delayElements(Duration.ofMillis(200))
                .doOnNext(volunteer -> {
                    String firstname= volunteer.getLastname();
                    String email = volunteer.getEmail();
                    System.out.println("Sending notification");
                    emailService.sendJoinWhatsAppNotification(firstname, memberCount, email);
                })
                .then().subscribe();
    }

    @Transactional
    private void authorizeActiveProjects(){
        List<Project> projects = projectRepo.findAllByStatus(ProjectStatus.OPEN);

        projects.forEach(project->{
            redisService.addAuthorizedUserProjects(project.getOrganization().getOrganizationId(), project.getProjectId());
            project.getApprovedList().forEach(application -> redisService.addAuthorizedUserProjects(application.getVolunteer().getVolunteerId(), project.getProjectId()));
        });
    }

    @PostConstruct
    public void start(){
        if(action.equals("send-join-notification")){
            sendNotificationToVolunteers();
        }

        if(action.equals("authorize-active-projects")){
            authorizeActiveProjects();
        }

    }

}
