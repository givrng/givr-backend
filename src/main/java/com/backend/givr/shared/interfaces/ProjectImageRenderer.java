package com.backend.givr.shared.interfaces;

import com.backend.givr.shared.dtos.RenderProjectDto;

public interface ProjectImageRenderer {
    byte[] renderProjectCard(RenderProjectDto project);
}
