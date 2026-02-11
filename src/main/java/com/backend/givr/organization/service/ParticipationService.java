package com.backend.givr.organization.service;

import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParticipationService {

    @Autowired
    private ParticipationRepo repo;

    public void createParticipation(Project project, Volunteer volunteer){
        Participation participation = new Participation();
        participation.setVolunteer(volunteer);
        participation.setProject(project);
        participation.setParticipationStatus(ParticipationStatus.IN_PROGRESS);
        repo.save(participation);
    }

    public List<Participation> getVolunteerParticipation(Volunteer volunteer){
        return repo.findAllByVolunteer(volunteer);
    }

    public void changeParticipationStatus(ParticipationStatus status, Long participationId){
        Participation participation = repo.findById(participationId).orElseThrow(()->new EntityNotFoundException(String.format("Participant with participationId %s, not not found", participationId)));

        if(status == ParticipationStatus.IN_PROGRESS)
            throw new IllegalOperationException("Illegal operation, cannot change a participation to in-progress");

        participation.setParticipationStatus(status);
        repo.save(participation);
    }

    public void deleteVolunteerParticipation(Long participationId, Volunteer volunteer){
        Participation participation = repo.findByIdAndVolunteer(participationId, volunteer).orElseThrow(()->new IllegalOperationException("Illegal operation, cannot perform operation delete"));
        repo.delete(participation);
    }
}
