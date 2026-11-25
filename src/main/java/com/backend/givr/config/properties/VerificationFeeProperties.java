package com.backend.givr.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "givr.verification")
public record VerificationFeeProperties(
        Double organizationVerificationFee,
        Double individualVerificationFee,
        Double initiativeCreationFee
) {
}
