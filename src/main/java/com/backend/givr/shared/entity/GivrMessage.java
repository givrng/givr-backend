package com.backend.givr.shared.entity;

import com.backend.givr.shared.dtos.MsgReceivedDto;
import com.backend.givr.shared.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(indexes = {@Index(name = "ind_sent_by", columnList = "project_id, user_id"), @Index(name = "ind_username", columnList = "email")} )
@NoArgsConstructor
public class GivrMessage {
    @Id
    private String msgId;
    @Column(length = 700)
    private String content;

    private Long projectId;
    // User Id
    @Column(name = "user_id")
    private String sentBy;

    @Column(name = "email")
    private String username;
    @Enumerated(EnumType.STRING)
    private AccountType role;
    private LocalDateTime sentAt;
    private LocalDateTime savedAt;

    public GivrMessage(MapRecord<String, Object, Object> entries) {
        this.msgId = entries.getId().getValue();
        Map<Object, Object> payload = entries.getValue();

        this.content = (String) payload.get("content");
        this.projectId = Long.parseLong((String) payload.get("projectId"));
        this.sentBy = (String) payload.get("sentBy");
        this.username = (String) payload.get("username");
        this.role = AccountType.valueOf((String) payload.get("role"));

        var timeSent = (String) payload.get("sentAt");
        this.sentAt = LocalDateTime.parse(timeSent);
    }

    @PrePersist
    private void setSavedAt(){
        this.savedAt = LocalDateTime.now();
    }

}
