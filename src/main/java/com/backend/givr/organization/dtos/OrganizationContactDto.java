package com.backend.givr.organization.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class OrganizationContactDto {
    private String contactFirstname;
    private String contactMiddleName;
    private String contactLastname;
    private String phoneNumber;
    private String email;

    private Boolean emailEditable;
    private Boolean emailVerified;
}
