package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SkillRepo extends JpaRepository<Skill, Long> {
    List<Skill> findByNameIn(Collection<String> names);
}
