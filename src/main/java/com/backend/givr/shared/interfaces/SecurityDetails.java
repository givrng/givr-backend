package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.shared.enums.VerificationStatus;
import org.springframework.security.core.userdetails.UserDetails;

public interface SecurityDetails extends UserDetails {
    String getId();
    void setAuthorities(VerificationStatus status);
    AuthProviderType getProviderType();
}
