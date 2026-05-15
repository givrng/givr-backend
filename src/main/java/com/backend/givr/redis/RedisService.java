package com.backend.givr.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;


public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void addAuthorizedUserProjects(String userId, Long projectId){
        // A SET of users that have the permission to connect to a group and allowed groups
        String AUTH_SET_KEY = "user:";
        redisTemplate.opsForSet()
                .add(AUTH_SET_KEY +userId, projectId);
    }

    public List<MapRecord<String, Object, Object>> readMessages(Duration duration, ReadOffset readOffset ){
        StreamReadOptions options = StreamReadOptions.empty()
                        .block(duration);
        // Stream where group messages are written to
        String MSG_STREAM = "stream:messages";
        StreamOffset<String> offset = StreamOffset.create(MSG_STREAM, readOffset);
        return redisTemplate.opsForStream()
                .read(options, offset);
    }

}
