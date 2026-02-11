package com.backend.givr.shared.mapper;

import com.backend.givr.shared.Skill;
import com.backend.givr.shared.SkillDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SharedMapper {
    SkillDto toSkillDto(Skill skill);
}
