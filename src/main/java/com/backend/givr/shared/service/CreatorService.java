package com.backend.givr.shared.service;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.repo.CreatorRepo;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CreatorService {
    private final CreatorRepo repo;

    public Creator createOrReturnCreator(Volunteer individual){
        return repo.findByIndividual(individual).orElseGet(()-> repo.save(new Creator(individual)));
    }

    public Creator getCreatorByIndividualId(String individualId){
        return repo.findByIndividualVolunteerId(individualId).orElseThrow(()-> new EntityNotFoundException("Creator does not exist"));
    }

    public Creator createOrReturnCreator(Organization organization){
        return repo.findByOrganization(organization).orElseGet(()-> repo.save(new Creator(organization)));
    }


}
