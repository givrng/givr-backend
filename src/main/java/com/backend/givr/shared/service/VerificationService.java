package com.backend.givr.shared.service;

import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.enums.ReviewStatus;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class VerificationService {

    @Autowired
    private EmailService emailService;
    @Autowired
    private OrganizationVerificationSessionRepo verificationSessionRepo;

    private final Logger logger = LoggerFactory.getLogger(VerificationService.class);

    public @Nullable List<OrganizationVerificationSession> getVerificationSessions(ReviewStatus status) {
        if(status == null)
            return verificationSessionRepo.findAll();
        
        Sort sort = Sort.by("createdAt" ).descending();
        return verificationSessionRepo.findByReviewStatus(status, sort);
    }

    public Boolean createVerificationSession(Organization organization, OrganizationUpdateDto organizationUpdateDto){
        if(organizationUpdateDto.getLocation() == null || organizationUpdateDto.getCacRegNumber() == null || organizationUpdateDto.getCacDocUrl() ==null)
            return false;

        OrganizationVerificationSession session = new OrganizationVerificationSession(organization, organizationUpdateDto);
        verificationSessionRepo.save(session);
        return true;
    }


}
