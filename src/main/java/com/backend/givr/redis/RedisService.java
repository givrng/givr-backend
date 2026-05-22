package com.backend.givr.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    public void addAuthorizedUserProjects(String userId, Long projectId){
        // A SET of users that have the permission to connect to a group and allowed groups
        String AUTH_SET_KEY = "user:";
        redisTemplate.opsForSet()
                .add(AUTH_SET_KEY +userId, projectId.toString());
    }

    public void removeAuthorizedUserProject(String userId, Long projectId){
        redisTemplate.opsForSet()
                .remove("user:"+userId, projectId.toString());
    }

    public List<MapRecord<String, Object, Object>> readMessages(Long count, ReadOffset readOffset) {
        StreamReadOptions options = StreamReadOptions.empty()
                .count(count)
                .block(Duration.ofSeconds(5));
        String MSG_STREAM = "stream:messages";

        List<MapRecord<String, Object, Object>> records = redisTemplate.opsForStream().read(options, StreamOffset.create(MSG_STREAM, readOffset));
        return records == null ? List.of() : records;
    }

    public List<MapRecord<String, Object, Object>> readUserProjectOffset(Long count, ReadOffset readOffset){
        StreamReadOptions options = StreamReadOptions.empty()
                .count(count)
                .block(Duration.ofSeconds(5));

        String POINTER_STREAM = "user_project_pointers";

        var records = redisTemplate.opsForStream().read(options, StreamOffset.create(POINTER_STREAM, readOffset));

        return records==null? List.of() : records;
    }

}
