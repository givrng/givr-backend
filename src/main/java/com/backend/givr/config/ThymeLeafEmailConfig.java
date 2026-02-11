package com.backend.givr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

@Configuration
@EnableAsync
public class ThymeLeafEmailConfig {

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
}
