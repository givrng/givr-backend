package com.backend.givr.volunteer.service;

import com.backend.givr.shared.enums.IDType;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.entity.VolunteerVerificationSession;
import com.backend.givr.volunteer.repo.VolunteerVerificationSessionRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VolunteerVerificationSessionService {
    @Autowired
    private VolunteerVerificationSessionRepo repo;

    public boolean existsByIdentification(String idNumber, IDType idType){
        return repo.existsByIdNumberAndIdType(idNumber, idType);
    }

    public void save(VolunteerVerificationSession session){
        repo.save(session);
    }

    public Optional<VolunteerVerificationSession> getVerificationSessionByVolunteer(Volunteer volunteer){
        return repo.findByVolunteer(volunteer);
    }
}
