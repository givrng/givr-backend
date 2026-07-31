package com.backend.givr.volunteer.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class VolunteerDetailsService implements UserDetailsService {
    @Autowired
    private VolunteerDetailsRepo repo;

    @Override
    public VolunteerDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findById(username).orElseThrow(()->new NoSuchElementException("Invalid credentials"));
    }

    public void save(VolunteerDetails details){
        if(details==null)
            return;

        repo.save(details);
    }
}
