package com.backend.givr.config;

import com.backend.givr.shared.entity.GivrMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String hostname;
    @Value("${spring.data.redis.port}")
    private int port;
    @Value("${spring.data.redis.password}")
    private String password;

    @Bean
    public RedisConnectionFactory redisConnectionFactory(){
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(hostname);
        configuration.setPort(port);
        if(password!=null)
            configuration.setPassword(password);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connection, ObjectMapper mapper){
        RedisTemplate<String, Object> template= new RedisTemplate<>();
        JacksonJsonRedisSerializer<Object> serializer = new JacksonJsonRedisSerializer<>(mapper, Object.class);
        template.setConnectionFactory(connection);

        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);


        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);

        return template;
    }

}
