package com.backend.givr;

import com.backend.givr.shared.email.EmailService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class Script {
    @Autowired
    private VolunteerRepo volunteerRepo;
    @Autowired
    private EmailService emailService;
    @Value("${app.action}")
    private String action;

    public void sendNotificationToVolunteers(){
        System.out.println("Executing task");
        List<Volunteer> volunteers = volunteerRepo.findAll();

        Flux<Volunteer> volunteerFlux = Flux.fromIterable(volunteers);

        volunteerFlux.delayElements(Duration.ofMillis(200))
                .doOnNext(volunteer -> {
                    String firstname= volunteer.getLastname();
                    String email = volunteer.getEmail();
                    emailService.sendGivrStory(firstname, email);
                })
                .then().subscribe();
    }


    @PostConstruct
    public void start(){
        if(action.equals("send-join-notification")){
            sendNotificationToVolunteers();
        }
    }

}
