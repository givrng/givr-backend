package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface VerificationClient {
    void verify(OrganizationVerificationSession session) throws JsonProcessingException;
}
