package com.backend.givr.shared.entity;

import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.shared.dtos.TransactionStatus;
import com.backend.givr.shared.entity.projects.Project;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity

@Getter
@NoArgsConstructor
public class ProjectPaymentReceipt extends BaseEntity {
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    private Creator creator;

    @ManyToOne(optional = false)
    private  Project project;

    @OneToOne
    @Setter
    @JoinColumn(name = "confirmed_payment")
    private VerificationPayment payment;

    @Setter
//    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    public ProjectPaymentReceipt(Creator creator, Project project, VerificationPayment payment){
        this.creator = creator;
        this.project = project;
        this.payment = payment;
        status = TransactionStatus.PENDING;
    }
}
