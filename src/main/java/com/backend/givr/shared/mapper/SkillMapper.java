package com.backend.givr.shared.mapper;

import com.backend.givr.shared.Skill;
import com.backend.givr.shared.dtos.SkillDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDto toSkillDto(Skill skill);
}
