package com.backend.givr.organization.entity;

import com.backend.givr.organization.service.verify.Address;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
public class CACResponse {
    @Embeddable
    @Getter
    public static class Summary{
        String cac_check;
    }
    @Embeddable
    @Getter
    public static class Status{
        @Column(name = "status_state")
        String state;
        String status;
    }

    @Embeddable
    @Getter
    public static class CAC {
        private String companyName;
        private String headOfficeAddress;
        @Embedded
        private Address branchAddress;
        private String rcNumber;
    }

    @Id
    @Setter
    private String id;

    @Setter
    private boolean isConfirmed;

    @Embedded
    private Summary summary;

    @Embedded
    private Status status;
    @Embedded
    private  CAC cacInfo;

    @PrePersist
    public void setIsConfirmed(){
        this.isConfirmed = false;
    }

}
