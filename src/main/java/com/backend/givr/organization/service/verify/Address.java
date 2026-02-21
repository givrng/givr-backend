package com.backend.givr.organization.service.verify;

import jakarta.persistence.Embeddable;

@Embeddable
public record Address (String address, String LGA, String state) {
}
