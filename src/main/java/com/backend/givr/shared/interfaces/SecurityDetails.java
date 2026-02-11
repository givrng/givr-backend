package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.enums.AuthProviderType;
import org.springframework.security.core.userdetails.UserDetails;

public interface SecurityDetails extends UserDetails {
    String getId();
    void setAuthorities();
    AuthProviderType getProviderType();
}
