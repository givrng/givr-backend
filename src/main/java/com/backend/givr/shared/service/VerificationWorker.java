package com.backend.givr.shared.service;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.shared.interfaces.VerificationClient;
import com.backend.givr.shared.repo.OrganizationVerificationSessionRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class VerificationWorker {
    @Autowired
    private QoreIdClient verificationClient;

    @Autowired
    private OrganizationVerificationSessionRepo verificationSessionRepo;

    private final Logger logger = LoggerFactory.getLogger(VerificationWorker.class);
//
//    value = Exception.class,
//    maxAttempts = 3,
//    backoff = @Backoff(delay = 2000)
    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000)
    )
    public void verifyContactPersonInformation(OrganizationVerificationSession verificationSession){
        try{
            verificationClient.verify(verificationSession);
        } catch (JsonProcessingException e) {
            logger.error("Verification for {} failed because of {}", verificationSession.getSessionId(), e.getLocalizedMessage());
        }finally {
            verificationSessionRepo.save(verificationSession);
        }
    }
}
