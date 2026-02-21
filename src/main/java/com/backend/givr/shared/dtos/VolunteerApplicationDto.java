package com.backend.givr.shared.dtos;

import com.backend.givr.organization.entity.ProjectApplication;
import com.backend.givr.shared.entity.Skill;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VolunteerApplicationDto {
    private String firstname;
    private String lastname;
    private List<String> skills;
    private ProjectAppliedDto projectApplied;

    public VolunteerApplicationDto(ProjectApplication application){
        this.firstname = application.getVolunteer().getFirstname();
        this.lastname = application.getVolunteer().getLastname();
        this.skills = application.getVolunteer().getSkills().stream().map(Skill::getName).toList();
        projectApplied = new ProjectAppliedDto(application);
    }
}
