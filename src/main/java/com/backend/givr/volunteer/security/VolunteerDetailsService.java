package com.backend.givr.volunteer.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class VolunteerDetailsService implements UserDetailsService {
    @Autowired
    private VolunteerDetailsRepo repo;

    @Override
    public VolunteerDetails loadUserByUsername(String username) {
        return repo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
    }

    public Optional<VolunteerDetails> getDetails(String username){
        return repo.findByEmail(username);
    }
    public void save(VolunteerDetails details){
        if(details==null)
            return;
        repo.save(details);
    }

    public void updatePassword(String password, String email){
        VolunteerDetails volunteerDetails = loadUserByUsername(email);
        volunteerDetails.setPassword(password);
        repo.save(volunteerDetails);
    }

    public boolean userExistsByEmail(String email) {
        return repo.existsByEmail(email);
    }
}
