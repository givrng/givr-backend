package com.backend.givr.redis;

import com.backend.givr.organization.service.OrganizationService;
import com.backend.givr.organization.service.ParticipationService;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.entity.GivrMessage;
import com.backend.givr.shared.entity.GivrUserProjectPointer;
import com.backend.givr.shared.enums.AccountType;
import com.backend.givr.shared.service.GivrMessageService;
import com.backend.givr.volunteer.service.VolunteerService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
public class GroupMsgListener {
    private final ExecutorService executorService;

    private final RedisService redisService;

    @Autowired
    private ObjectMapper mapper;
    @Autowired
    private GivrMessageService messageService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private VolunteerService volunteerService;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private ParticipationService participationService;

    @Autowired
    private EmailService emailService;

    // Static variable
    private static final AtomicReference<ReadOffset> lastProcessedMsgRecord = new AtomicReference<>();
    private static final AtomicReference<ReadOffset> lastProcessProjectRecord = new AtomicReference<>();
    private static final Logger logger = LoggerFactory.getLogger(GroupMsgListener.class);
    //------------ end ----------------
    public GroupMsgListener(@Qualifier("executorService") ExecutorService executorService, @Autowired RedisService redisService){
        this.executorService = executorService;
        this.redisService = redisService;
    }

    public void sendNotification(GivrMessage msg) {

        Mono<String> orgName = Mono.fromCallable(() ->
                        organizationService.getOrganizationName(msg.getSentBy()))
                .subscribeOn(Schedulers.boundedElastic());

        Mono<String> projectTitle = Mono.fromCallable(() ->
                        projectService.getProjectTitle(msg.getProjectId()))
                .subscribeOn(Schedulers.boundedElastic());


        Mono.zip(orgName, projectTitle)   // fetch both concurrently
                .flatMapMany(tuple -> {
                    String org = tuple.getT1();
                    String title = tuple.getT2();

                    return Flux.fromIterable(
                                    participationService.getVolunteerParticipationEmail(msg.getProjectId()))
                            .delayElements(Duration.ofMillis(200))
                            .doOnNext(System.out::println)
                            .concatMap(volunteer -> Mono.fromCallable(() -> {
                                                emailService.sendChatNotification(
                                                        volunteer, org, title, msg.getContent());
                                                return volunteer;
                                            })
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .doOnSuccess(v ->
                                                    log.info("Chat notification sent to {}", v))
                                            .onErrorResume(ex -> {
                                                log.error("Failed to notify {}: {}",
                                                        volunteer, ex.getMessage());
                                                return Mono.empty();
                                            })
                            );
                })
                .then().subscribe();
    }

    @PostConstruct
    public void listenForMessages(){
        if(lastProcessedMsgRecord.get() == null){
            GivrMessage msg = messageService.getMsgOffset();
            // if msg is null, no message has been processed
            // Otherwise start from the last processed message
            lastProcessedMsgRecord.set(msg==null? ReadOffset.from("0"): ReadOffset.from(msg.getMsgId()));
        }

        executorService.submit(() -> {
            while (true) {
                List<MapRecord<String, Object, Object>> records = redisService.readMessages(10L, lastProcessedMsgRecord.get());

                Flux.fromIterable(records)
                        .map(GivrMessage::new)
                        .filter(msg->msg.getRole().equals(AccountType.ORGANIZATION))
                        .doOnNext(msg-> System.out.println(msg.getRole()))
                        .subscribe(this::sendNotification);
                try {
                    List<GivrMessage> msgs = records.stream().map(GivrMessage::new).toList();
                    if(!msgs.isEmpty()){
                        List<GivrMessage> saved= messageService.saveAllMessage(msgs);
                        lastProcessedMsgRecord.set(ReadOffset.from(saved.getLast().getMsgId()));
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage());
                    System.err.println(e.getMessage());
                }
            }
        });
    }

    @PostConstruct
    public void listenForOffSets(){
        if(lastProcessProjectRecord.get() == null){
            GivrUserProjectPointer projectOffset = messageService.getLastOffsetValue();
            lastProcessProjectRecord.set(projectOffset == null? ReadOffset.from("0"): ReadOffset.from(projectOffset.getRecordId()));
        }

        executorService.submit(()->{
            while (true){
                List<MapRecord<String, Object, Object>> records = redisService.readUserProjectOffset(10L, lastProcessProjectRecord.get());

                List<GivrUserProjectPointer> pointers = messageService.saveAllProjectPointer(records.stream().map(GivrUserProjectPointer::new).toList());
                if(!pointers.isEmpty())
                    lastProcessProjectRecord.set(ReadOffset.from(pointers.getLast().getRecordId()));
            }
        });
    }

    @PreDestroy
    public void endThread(){
        executorService.close();
    }
}
