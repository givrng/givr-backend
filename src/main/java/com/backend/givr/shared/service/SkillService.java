package com.backend.givr.shared.service;

import com.backend.givr.shared.Skill;
import com.backend.givr.shared.repo.SkillRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SkillService {
    @Autowired
    private SkillRepo repo;

    public Set<Skill> updateSkills(List<String> skills){
        List<Skill> existingSkills = repo.findByNameIn(skills);

        // If all skills exist in the database, save
        if(existingSkills.size() == skills.size()){
            return new HashSet<>(existingSkills);
        }
        Set<String> existingSkillNames = existingSkills.stream().map(Skill::getName).collect(Collectors.toSet());
        Set<Skill> newSkills = skills.stream().filter(skill-> !existingSkillNames.contains(skill)).map(Skill::new).collect(Collectors.toSet());

        newSkills.forEach(skill -> System.out.println(skill.getName()));

        var saved = repo.saveAll(newSkills);

        return  Stream.concat(existingSkills.stream(), saved.stream()).collect(Collectors.toSet());
    }
}
