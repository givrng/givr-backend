package com.backend.givr;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.service.ProjectAsyncServiceWorker;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.enums.ProjectStatus;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Script implements Runnable{
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ProjectAsyncServiceWorker worker;
    @Override
    public void run() {
        System.out.println("Script is running");
        List<Project> projects = projectService.getAllProjectsByStatus(ProjectStatus.OPEN);

        projects.forEach(project -> worker.sendProjectListing(project));
    }

    public void start(){
        run();
    }
}
