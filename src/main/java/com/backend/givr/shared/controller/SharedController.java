package com.backend.givr.shared.controller;

import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.email.ThymeleafTemplateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/${api.version}/share/project/{id}")
public class SharedController {
    @Autowired
    private ProjectService projectService;

    @Value("${client.app.baseUrl}")
    private String clientBaseUrl;

    @Autowired
    private ThymeleafTemplateService templateService;

    @GetMapping
    public ResponseEntity<String> share(@PathVariable("id") Long projectId, HttpServletRequest req){
        Project project = projectService.getProject(projectId);
        String html = templateService.projectCard(project.getTitle(), project.getDescription(), project.getProjectCardUrl(),
                String.format("%s/volunteer?project=%s", clientBaseUrl, project.getProjectId()), req.getRequestURL().toString());
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }
}
