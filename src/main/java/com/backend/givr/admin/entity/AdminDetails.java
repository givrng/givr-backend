package com.backend.givr.admin.entity;

import com.backend.givr.shared.enums.AuthProviderType;
import com.backend.givr.shared.interfaces.SecurityDetails;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

public class AdminDetails implements SecurityDetails {
    private List<SimpleGrantedAuthority> authorities;
    private String username;
    private String id;

    public AdminDetails(Admin admin){
        this.username = admin.getEmail();
        this.id = admin.getAdminId();
        this.authorities = List.of(new SimpleGrantedAuthority(admin.getRole().name()));
    }

    public AdminDetails(String username, String id, String authority){
        this.username = username;
        this.authorities =  List.of(new SimpleGrantedAuthority(authority));
        this.id = id;
    }
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setAuthorities() {

    }

    @Override
    public AuthProviderType getProviderType() {
        return null;
    }

    @Override
    public @NotNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return "";
    }

    @Override
    public @NotNull String getUsername() {
        return username;
    }
}
