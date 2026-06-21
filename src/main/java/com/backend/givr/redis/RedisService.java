package com.backend.givr.redis;

import jakarta.annotation.PostConstruct;
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

    @PostConstruct
    private void createGroup(){
        try{
            redisTemplate.opsForStream()
                    .createGroup("user_project_pointers", ReadOffset.from("0-0"), "pointer-group" );
        }catch (Exception ignored){

        }
    }

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

    public List<MapRecord<String, Object, Object>> readUserProjectOffset(long count){
        StreamReadOptions options = StreamReadOptions.empty()
                .count(count)
                .block(Duration.ofSeconds(5));

        String POINTER_STREAM = "user_project_pointers";

        var records = redisTemplate.opsForStream().read(Consumer.from("pointer-group", "listener-1"),
                options,
                StreamOffset.create(POINTER_STREAM, ReadOffset.lastConsumed()));

        return records==null? List.of() : records;
    }

    public void acknowledgeRecord(String recordId){
        redisTemplate.opsForStream()
                .acknowledge("user_project_pointers", "pointer-group", recordId);

    }
    public List<String> getProjectParticipantsEmails(Long projectId){
        return redisTemplate.opsForSet()
                .members("participants:"+projectId)
                .stream().map(String::valueOf).toList();
    };

    public void addProjectParticipantEmail(Long projectId, List<String> emails){
        emails.forEach(email->redisTemplate.opsForSet().add("participants:"+projectId, email));
    }
}
