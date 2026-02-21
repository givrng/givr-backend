package com.backend.givr.organization.security;

import com.backend.givr.organization.entity.Organization;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.enums.AuthProviderType;
import jakarta.validation.constraints.Email;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OrganizationDetailsService implements UserDetailsService {
    @Autowired
    private OrganizationDetailsRepo repo;
    @Override
    public OrganizationDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByEmail(username).orElseThrow(()->new UsernameNotFoundException("Invalid credentials"));
    }


    public void save(OrganizationDetails details){
        if(details == null)
            return;
        repo.save(details);
    }

    public void updatePassword(String newPassword, String email) {
        OrganizationDetails organizationDetails = loadUserByUsername(email);
        organizationDetails.setPassword(newPassword);
        repo.save(organizationDetails);
    }
    @Transactional
    public void updateEmail (@Email String newEmail, String oldEmail){
        OrganizationDetails details = loadUserByUsername(oldEmail);
        if(details.getAuthProvider() == AuthProviderType.LOCAL)
            details.setEmail(newEmail);
        else
            throw new IllegalOperationException("Cannot change email, social media sign in");
    }

    public boolean emailExist(String email){
        return repo.existsByEmail(email);
    }

    public Optional<OrganizationDetails> getDetails(String email) {
        return repo.findByEmail(email);
    }

    public String getEmail(Organization organization) {
        return repo.findByOrganization(organization).getUsername();
    }
}
