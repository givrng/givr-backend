package com.backend.givr.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record AttendanceHours (@Column(name = "from_time") String from, @Column(name = "to_time") String to){
}
