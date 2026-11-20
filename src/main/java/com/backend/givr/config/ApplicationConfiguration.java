package com.backend.givr.config;

import com.backend.givr.shared.enums.MediaUploadPurpose;
import com.cloudinary.Cloudinary;
import com.cloudinary.api.exceptions.AlreadyExists;
import com.cloudinary.utils.ObjectUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.net.Proxy;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@Slf4j
public class ApplicationConfiguration {

    @Value("${cloudinary.cloud-name}")
    private String cloudinaryCloudName;
    @Value("${cloudinary.api.key}")
    private String cloudinaryApiKey;
    @Value("${cloudinary.api.secret}")
    private String cloudinaryApiSecret;
    @Bean
    public SpringTemplateEngine emailTemplateEngine(){
        SpringTemplateEngine engine = new SpringTemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();

        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);

        engine.setTemplateResolver(resolver);
        return engine;
    }

    @Bean
    public ObjectMapper objectMapper (){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
    @Bean
    public RestTemplate restTemplate(){
        var factory = new SimpleClientHttpRequestFactory();
        factory.setProxy(Proxy.NO_PROXY);
        return new RestTemplate(factory);
    }

    @Bean
    public Cloudinary cloudinary(){

        return new Cloudinary(Map.of(
                "cloud_name", cloudinaryCloudName,
                "api_key", cloudinaryApiKey,
                "api_secret", cloudinaryApiSecret
        ));
    }
    @Bean(name = "executorService")
    ExecutorService executorService(){
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    @ConditionalOnBooleanProperty(prefix = "cloudinary.preset", name = "sync")
    public ApplicationRunner createCloudinaryPresets(Cloudinary cloudinary) throws Exception {
        return args -> {
            for(MediaUploadPurpose purpose: MediaUploadPurpose.values())
                sync(purpose, cloudinary);
        };
    }

    private void sync(MediaUploadPurpose purpose, Cloudinary cloudinary) throws Exception {
        final String name = purpose.getUploadPreset();
        final Map<String, Object> settings = ObjectUtils.asMap(
                "unsigned", false,                       // signed uploads only
                "folder", purpose.getFolder(),
                "allowed_formats", String.join(",", purpose.getAllowedFormats()),
                "disallow_public_id", true,              // client can't choose the public_id
                "use_filename", false,
                "unique_filename", true,
                "overwrite", false
        );

        try {
            final Map<String, Object> createOptions = ObjectUtils.asMap("name", name);
            createOptions.putAll(settings);
            cloudinary.api().createUploadPreset(createOptions);
            log.info("Created Cloudinary upload preset {}", name);
        } catch (AlreadyExists e) {
            cloudinary.api().updateUploadPreset(name, settings);
            log.info("Updated Cloudinary upload preset {}", name);
        }
    }
}
