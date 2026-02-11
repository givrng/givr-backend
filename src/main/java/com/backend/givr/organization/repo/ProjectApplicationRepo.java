package com.backend.givr.organization.repo;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.organization.entity.Project;
import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.shared.enums.ApplicationStatus;
import com.backend.givr.volunteer.entity.Volunteer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProjectApplicationRepo extends JpaRepository<ProjectApplication, Long> {
    void deleteByProjectAndVolunteer(Project project, Volunteer volunteer);

    Optional<ProjectApplication> findByProjectAndVolunteer(Project project, Volunteer volunteer);

    List<ProjectApplication> findAllByVolunteer(Volunteer volunteer);

    List<ProjectApplication> findAllByOrganizationAndStatus(Organization organization, ApplicationStatus status);
}
