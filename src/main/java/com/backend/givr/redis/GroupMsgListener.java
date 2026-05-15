package com.backend.givr.redis;

import com.backend.givr.shared.dtos.MsgReceivedDto;
import com.backend.givr.shared.entity.GivrMessage;
import com.backend.givr.shared.repo.GivrMesssageRepo;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.stream.ReadOffset;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class GroupMsgListener {
    private final ExecutorService executorService;
    private final RedisService redisService;

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private GivrMesssageRepo repo;

    // Static variable
    private static ReadOffset lastProcessedMsgRecord = null;
    //------------ end ----------------
    public GroupMsgListener(@Qualifier("executorService") ExecutorService executorService, @Autowired RedisService redisService){
        this.executorService = executorService;
        this.redisService = redisService;
    }

    @PostConstruct
    public void listenForMessages(){
        if(lastProcessedMsgRecord == null){
            GivrMessage msg = repo.findFirstBy(Sort.by(Sort.Order.desc("savedAt"))).orElse(null);
            // if msg is null, no message has been processed
            // Otherwise start from the last processed message
            lastProcessedMsgRecord = msg==null? ReadOffset.latest(): ReadOffset.from(msg.getMsgId());
        }

        executorService.submit(()->{
            while (true){
                String lastProcessedMsgId = repo.saveAll(
                        redisService.readMessages(Duration.ofSeconds(3), lastProcessedMsgRecord).stream().map(record->{
                                    Map<Object, Object> value = record.getValue();
                                    MsgReceivedDto msgReceivedDto = mapper.convertValue(value, MsgReceivedDto.class);
                                    return new GivrMessage(record.getId(), msgReceivedDto);
                                })
                                .toList()
                ).getLast().getMsgId();

                lastProcessedMsgRecord = ReadOffset.from(lastProcessedMsgId);
            }
        });
    }
}
