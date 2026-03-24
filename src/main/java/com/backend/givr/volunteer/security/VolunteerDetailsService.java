package com.backend.givr.volunteer.security;

import com.backend.givr.volunteer.entity.Volunteer;
import com.backend.givr.volunteer.repo.VolunteerRepo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class VolunteerDetailsService implements UserDetailsService {
    @Autowired
    private VolunteerRepo repo;

    @Override
    public @NotNull VolunteerDetails loadUserByUsername(String username) {
        var volunteer = repo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
        return new VolunteerDetails(volunteer);
    }

    public Optional<Volunteer> getDetails(String username){
        return repo.findByEmail(username);
    }

    public void updatePassword(String password, String email){
        Volunteer volunteerDetails = repo.findByEmail(email).orElseThrow();
        volunteerDetails.setPassword(password);
        repo.save(volunteerDetails);
    }

    public boolean userExistsByEmail(String email) {
        return repo.existsByEmail(email);
    }
}
