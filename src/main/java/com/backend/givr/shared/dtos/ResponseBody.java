package com.backend.givr.shared.dtos;

public record ResponseBody(
        String transactionReference,
        String checkoutUrl
) {
}
