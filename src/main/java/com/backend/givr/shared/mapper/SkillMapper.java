package com.backend.givr.shared.mapper;

import com.backend.givr.shared.entity.Skill;
import com.backend.givr.shared.dtos.SkillDto;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SkillMapper {
    SkillDto toSkillDto(Skill skill);

    default Skill toSkill(String name){
        return new Skill(name);
    }

    List<Skill> toSkills(List<String> names);
}
