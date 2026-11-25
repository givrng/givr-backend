package com.backend.givr.shared.repo;

import com.backend.givr.organization.entity.VerificationPayment;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.ProjectPaymentReceipt;
import com.backend.givr.shared.entity.projects.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProjectPaymentReceiptRepo extends JpaRepository<ProjectPaymentReceipt, String> {
    Optional<ProjectPaymentReceipt> findByCreatorAndProject(Creator creator, Project project);

    Optional<ProjectPaymentReceipt> findByPayment(VerificationPayment verificationPayment);
}
