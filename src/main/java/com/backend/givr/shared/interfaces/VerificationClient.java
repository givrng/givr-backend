package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.entity.OrganizationVerificationSession;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface VerificationClient {
    void verifyOrganization(OrganizationVerificationSession session) throws JsonProcessingException;
    void verifyVolunteer(VolunteerVerificationSession session) throws JsonProcessingException;
}
