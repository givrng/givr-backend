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
        int count = 0;
        
        for(Project project: projects){
            worker.sendProjectListing(project);
            count++;

            if(count % 5 == 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
    }

    @PostConstruct
    public void start(){
        run();
    }
}
