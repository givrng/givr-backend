package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.AccountType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MsgReceivedDto {
    private Integer projectId;
    private String content;

    private String sendBy;
    private String username;
    private AccountType role;
    private LocalDateTime sentAt;
}
