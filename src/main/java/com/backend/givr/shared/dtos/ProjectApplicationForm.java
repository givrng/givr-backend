package com.backend.givr.shared.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class ProjectApplicationForm {
    @NotNull Long projectId;
    @NotBlank String reason;
    @NotBlank String aboutMe;
    @NotNull Boolean isAvailable;
    List<String> mySkills;
    String additionalInfo;
}
