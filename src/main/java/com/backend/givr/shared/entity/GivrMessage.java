package com.backend.givr.shared.entity;

import com.backend.givr.shared.dtos.MsgReceivedDto;
import com.backend.givr.shared.enums.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.connection.stream.RecordId;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(indexes = {@Index(name = "ind_sent_by", columnList = "project_id, user_id"), @Index(name = "ind_username", columnList = "email")} )
public class GivrMessage {
    @Id
    private String msgId;
    private String content;

    private Integer projectId;
    // User Id
    @Column(name = "user_id")
    private String sentBy;

    @Column(name = "email")
    private String username;
    private AccountType role;
    private LocalDateTime sentAt;
    private LocalDateTime savedAt;

    @PrePersist
    private void setSavedAt(){
        this.savedAt = LocalDateTime.now();
    }

   public GivrMessage(RecordId key, MsgReceivedDto payload){
        this.msgId = key.getValue();
        this.content = payload.getContent();
        this.sentBy = payload.getSendBy();
        this.role = payload.getRole();
        this.projectId = payload.getProjectId();
        this.sentAt = LocalDateTime.from(payload.getSentAt());

   }
}
