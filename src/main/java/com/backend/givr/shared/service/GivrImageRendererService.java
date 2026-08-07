package com.backend.givr.shared.service;

import com.backend.givr.shared.dtos.RenderCertificateDto;
import com.backend.givr.shared.dtos.RenderProjectDto;
import com.backend.givr.shared.interfaces.GivrImageRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;

@Service
public class GivrImageRendererService implements GivrImageRenderer {

    @Autowired
    private RestTemplate restTemplate;
    @Value("${givr.renderer.baseUrl}")
    private String givrRendererBaseUrl;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public byte[] renderProjectCard(RenderProjectDto project) {
        try{
            return restTemplate.postForObject(String.format("%s/render/project", givrRendererBaseUrl), project, byte[].class);
        } catch (RestClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] renderCertificate(RenderCertificateDto certificateDto) {
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(certificateDto));
        try{
            return restTemplate.postForObject(String.format("%s/rebder/certificate", givrRendererBaseUrl), certificateDto, byte[].class);
        }catch (RestClientException e){
            throw new RuntimeException(e);
        }
    }
}
