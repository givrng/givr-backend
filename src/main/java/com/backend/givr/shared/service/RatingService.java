package com.backend.givr.shared.service;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.organization.service.ProjectService;
import com.backend.givr.shared.entity.Rating;
import com.backend.givr.shared.enums.ParticipationStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.repo.RatingRepo;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RatingService {
    @Autowired
    private RatingRepo repo;
    @Autowired
    private ParticipationRepo participationRepo;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private EntityManager entityManager;
    /**
     * Save or update a rating for a project by a volunteer.
     * Updates project cache and evicts organization cache to maintain consistency.
     */
    @Transactional
    @Caching(
            put = @CachePut(value = "projectRating", key = "#project.projectId"),
            evict = @CacheEvict(value = "organizationRating", key = "#project.organization.organizationId")
    )
    public void addOrUpdateRating(Volunteer volunteer, Long projectId, Double ratingScore){
        Project project = entityManager.getReference(Project.class, projectId);
        Participation participation = participationRepo.findByVolunteerAndProject(volunteer, project)
                .orElseThrow(()->new EntityNotFoundException(String.format("Volunteer %s is not a participant of %s project", volunteer.getVolunteerId(), project.getProjectId())));

        if(participation.getParticipationStatus() != ParticipationStatus.COMPLETED)
            throw new IllegalOperationException("Cannot review a project you have not completed");
        Optional<Rating> optionalRating = repo.findByVolunteerAndProject(volunteer, project);
        Rating rating = optionalRating.orElseGet(()-> new Rating(volunteer, project, ratingScore));
        rating.setRating(ratingScore);
        repo.save(rating);
        double projectRating = repo.getProjectRating(project);
        project.setRating(projectRating);
        projectService.save(project);
        // Compute updated average for the project (cached via @CachePut)
    }

    /**
     * Get the cached average rating for an organization.
     * Returns 0 if no ratings exist.
     */
    @Cacheable(value = "organizationRating", key = "#organization.organizationId")
    public double getOrganizationRatingScore(Organization organization){
        Double score = repo.getOrganizationRating(organization);
        return score == null? 0.0 : score;
    }

}
