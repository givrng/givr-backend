package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.dtos.RenderCertificateDto;
import com.backend.givr.shared.dtos.RenderProjectDto;

public interface GivrImageRenderer {
    byte[] renderProjectCard(RenderProjectDto project);
    byte[] renderCertificate(RenderCertificateDto certificateDto);
}
