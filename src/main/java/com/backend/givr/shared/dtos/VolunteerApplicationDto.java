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
    private String reason;
<<<<<<< HEAD
=======
    private Boolean isAvailable;
    private List<String> specialSkills;
    private String aboutVolunteer;
    private String additionalInfo;
    private String profileUrl;
>>>>>>> test
    private ProjectAppliedDto projectApplied;

    public VolunteerApplicationDto(ProjectApplication application){
        this.firstname = application.getVolunteer().getFirstname();
        this.lastname = application.getVolunteer().getLastname();
        this.skills = application.getVolunteer().getSkills().stream().map(Skill::getName).toList();
        this.reason = application.getApplicationReason();
<<<<<<< HEAD
=======
        this.aboutVolunteer = application.getAboutVolunteer();
        this.specialSkills = application.getSpecialSkills().stream().map(Skill::getName).toList();
        this.isAvailable = application.getIsAvailable();
        this.additionalInfo = application.getAdditionalInfo();
        this.profileUrl = application.getVolunteer().getProfileUrl();
>>>>>>> test
        projectApplied = new ProjectAppliedDto(application);
    }
}
