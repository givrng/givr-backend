package com.backend.givr.shared.entity;

import com.backend.givr.shared.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class GivrUserProjectPointer {
    @Id
    private String userId;
    private String recordId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "collection_project_offset", joinColumns = @JoinColumn(name = "project_offset"))
    @MapKeyColumn(name = "project_id")
    @Column(name = "offset_id")
    private Map<Long, String> projectOffsets = new HashMap<>();

    @Enumerated(EnumType.STRING)
    private AccountType role;

    private LocalDateTime createdAt;

    @PrePersist
    private void setCreatedAt(){
        this.createdAt = LocalDateTime.now(ZoneId.of("Africa/Lagos"));
    }

    public GivrUserProjectPointer(MapRecord<String, Object, Object> record){
        Map<Object, Object> payload = record.getValue();

        this.recordId = record.getId().getValue();
        this.userId = (String) payload.get("userId");
        payload.remove("userId");
        this.role = AccountType.valueOf((String) payload.get("role"));
        payload.remove("role");

        payload.entrySet().stream().forEach(entry->{
            Long projectId = Long.valueOf(entry.getKey().toString());
            String offsetId = entry.getValue() == null? "": entry.getValue().toString();
            projectOffsets.put(projectId, offsetId);
        });
    }
}
