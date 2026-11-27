package com.backend.givr.volunteer.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PublicVolunteerDto {
    private String volunteerId;
    private String firstname;
    private String middleName;
    private String lastname;
}
