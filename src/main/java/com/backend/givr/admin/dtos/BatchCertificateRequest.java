package com.backend.givr.admin.dtos;

import java.util.List;

public record BatchCertificateRequest(
        List<Long> participants
) {
}
