package com.backend.givr.organization.service;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.service.VolunteerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectAsyncServiceWorker {
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private EmailService emailService;

    @Async
    public void sendProjectListing( Project project){
        List<Volunteer> volunteers = volunteerService.getAllByLocation(project.getLocation());
        volunteers
                .forEach(volunteer -> emailService.sendProjectListingNotification(project, volunteer.getFirstname(), volunteer.getEmail()));
    }
}
