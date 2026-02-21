package com.backend.givr.admin.service;

import com.backend.givr.admin.entity.Admin;
import com.backend.givr.admin.entity.AdminDetails;
import com.backend.givr.admin.repos.AdminRepo;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class AdminDetailsService implements UserDetailsService {
    @Autowired
    private AdminRepo repo;

    @Override
    public @NotNull AdminDetails loadUserByUsername(@NotNull String username) throws UsernameNotFoundException {
        Admin admin = repo.findByEmail(username).orElseThrow();
        return new AdminDetails(admin);
    }
}

