package com.backend.givr.redis;

import com.backend.givr.shared.entity.GivrUserProjectPointer;
import com.backend.givr.shared.service.GivrMessageService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ReadOffSetListener {

    private final ExecutorService executorService;
    private final RedisService redisService;

    @Autowired
    private GivrMessageService messageService;

    public static final AtomicReference<ReadOffset> lastProcessedProjectRecord = new AtomicReference<>();

    public ReadOffSetListener(@Qualifier("executorService") ExecutorService executorService, @Autowired RedisService redisService){
        this.executorService = executorService;
        this.redisService = redisService;
    }

    @PostConstruct
    public void listenForOffSets(){
        if(lastProcessedProjectRecord.get() == null){
            GivrUserProjectPointer projectOffset = messageService.getLastOffsetValue();
            lastProcessedProjectRecord.set(projectOffset == null? ReadOffset.from("0-0"): ReadOffset.from(projectOffset.getRecordId()));
        }

        executorService.submit(()->{
            while (true){
                List<MapRecord<String, Object, Object>> records = redisService.readUserProjectOffset(10L);
                try{
                    List<GivrUserProjectPointer> pointers = messageService.saveAllProjectPointer(records.stream().map(record->{
                        var pointer = new GivrUserProjectPointer(record);
                        redisService.acknowledgeRecord(pointer.getRecordId());
                        return pointer;
                    }).toList());
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }

//                if(!pointers.isEmpty())
//                    lastProcessedProjectRecord.set(ReadOffset.from(pointers.getLast().getRecordId()));
            }
        });
    }
}
