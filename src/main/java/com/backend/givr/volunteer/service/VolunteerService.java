package com.backend.givr.volunteer.service;

import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.organization.service.ApplicationService;
import com.backend.givr.shared.Location;
import com.backend.givr.shared.ProjectApplicationForm;
import com.backend.givr.shared.repo.SkillRepo;
import com.backend.givr.shared.service.LocationService;
import com.backend.givr.shared.service.SkillService;
import com.backend.givr.volunteer.dtos.CreateVolunteerRequestDto;
import com.backend.givr.volunteer.dtos.UpdateVolunteerDto;
import com.backend.givr.volunteer.dtos.VolunteerDashboard;
import com.backend.givr.volunteer.dtos.VolunteerProfile;
import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.mappings.VolunteerMapper;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import com.backend.givr.volunteer.security.VolunteerDetails;
import com.backend.givr.volunteer.security.VolunteerDetailsService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VolunteerService {
    private final Logger logger = LoggerFactory.getLogger(VolunteerService.class);
    @Autowired
    private VolunteerRepo repo;
    @Autowired
    private VolunteerDetailsService detailsService;
    @Autowired
    private VolunteerMapper mapper;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private SkillService skillService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private SkillRepo skillRepo;
    @PersistenceContext
    private EntityManager manager;

    public Volunteer getVolunteer(String id){
        return repo.findById(id).orElseThrow();
    }

    public Volunteer createVolunteer(CreateVolunteerRequestDto volunteerDto){
        if(!volunteerDto.validatePassword())
            throw new IllegalStateException("Password and confirm password do not match");

        Volunteer volunteer = mapper.toVolunteer(volunteerDto);
        Location location = locationService.createLocation(volunteer.getLocation());
        volunteer.getLocation().setId(location.getId());
        return updateSkills(volunteer, volunteerDto.getInterests());
    }

    public VolunteerDashboard getVolunteerDashboard(String volunteerId){
        Volunteer volunteer = manager.getReference(Volunteer.class, volunteerId);
        List<ProjectApplication> applications = applicationService.getAppliedProjects(volunteer);

        return new VolunteerDashboard(volunteer.getFirstname(), applications);
    }

    public VolunteerProfile getVolunteerProfile(String volunteerId){
        return mapper.toProfile(getVolunteer(volunteerId));
    }
    public void createAuthPrincipal (CreateVolunteerRequestDto volunteerDto, Volunteer volunteer){
        VolunteerDetails details = new VolunteerDetails(volunteerDto.getEmail(), encoder.encode(volunteerDto.getPassword()), volunteer);
        detailsService.save(details);
    }

    @Transactional
    public Volunteer createAccount(CreateVolunteerRequestDto volunteerDto){
        try{
            var volunteer = createVolunteer(volunteerDto);
            createAuthPrincipal(volunteerDto, volunteer);
            return volunteer;
        }catch (IllegalStateException e){
            logger.error("Failed to create account for user: {}", e.getLocalizedMessage());
            return null;
        }
    }

    @Transactional
    public Volunteer updateSkills(Volunteer volunteer, List<String> skills){
        var updateSkills = skillService.updateSkills(skills);
        volunteer.setSkills(updateSkills);

       return repo.save(volunteer);
    }


    public Volunteer updateProfile(String volunteerId, UpdateVolunteerDto updateVolunteerDto){
        Volunteer volunteer = manager.getReference(Volunteer.class, volunteerId);
        volunteer.setLocation(locationService.createLocation(volunteer.getLocation()));
        mapper.updateVolunteer(updateVolunteerDto, volunteer);
        return repo.save(volunteer);
    }

    public void apply(String id, @Valid ProjectApplicationForm applicationForm) {
        Volunteer volunteer = manager.getReference(Volunteer.class, id);
        applicationService.apply(volunteer, applicationForm);
    }
}
