package com.backend.givr.shared.service;

import com.backend.givr.shared.dtos.RenderProjectDto;
import com.backend.givr.shared.interfaces.ProjectImageRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class RenderProjectService implements ProjectImageRenderer {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${givr.renderer.baseUrl}")
    private String projectRendererBaseUrl;

    @Override
    public byte[] renderProjectCard(RenderProjectDto project) {
        try{
            return restTemplate.postForObject(String.format("%s/render/project", projectRendererBaseUrl ), project, byte[].class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
    }
}
