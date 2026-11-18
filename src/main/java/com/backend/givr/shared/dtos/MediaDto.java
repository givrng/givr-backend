package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.MediaType;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record MediaDto(@NotNull MediaType mediaType, @NotNull @URL(protocol = "https") String mediaUrl) {
}
