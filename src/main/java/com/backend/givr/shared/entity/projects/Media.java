package com.backend.givr.shared.entity.projects;

import com.backend.givr.shared.entity.BaseEntity;
import com.backend.givr.shared.enums.MediaStatus;
import com.backend.givr.shared.enums.MediaType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Entity
@Getter
public class Media extends BaseEntity {
    private String url;
    @Enumerated(EnumType.STRING)
    private MediaType mediaType;


    @Enumerated(EnumType.STRING)
    private MediaStatus status;

    public Media(String url){
        this.mediaType = MediaType.Video;
        this.status = MediaStatus.Pending_Approval;
        this.url = url;
    }

}
