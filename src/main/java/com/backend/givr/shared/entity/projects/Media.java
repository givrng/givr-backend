package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.enums.MediaStatus;
import com.backend.givr.shared.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Getter
public class Media extends BaseEntity {
    private String url;
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    @OneToOne(optional = false, fetch =  FetchType.LAZY)
    @JoinColumn(name = "initiative_id", nullable = false)
    private Initiative initiative;

    @Enumerated(EnumType.STRING)
    private MediaStatus status;

    public Media(String url, Initiative initiative){
        this.mediaType = MediaType.Video;
        this.status = MediaStatus.Pending_Approval;
        this.initiative = initiative;
        this.url = url;
    }

}
