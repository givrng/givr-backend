package com.backend.givr.admin.entity;

import com.backend.givr.admin.enums.AdminRole;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String adminId;

    @Column(unique = true)
    private String email;

    @Setter
    private Boolean isAdmin;

    @Setter
    @Enumerated(EnumType.STRING)
    private AdminRole role;

    private Admin(String email, AdminRole role){
        this.email = email;
        this.role = role;
        this.isAdmin = true;
    }

    public static Admin createAdmin(String email, AdminRole role){
        return new Admin(email, role);
    }
}
