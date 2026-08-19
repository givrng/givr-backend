package com.backend.givr.shared.mapper;

import com.backend.givr.shared.dtos.VolunteerCertificateDto;
import com.backend.givr.shared.entity.VolunteerCertificate;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;


@Mapper(componentModel = "spring")
public interface CertificateMapper {
    VolunteerCertificateDto toDto(VolunteerCertificate volunteerCertificate);
    List<VolunteerCertificateDto> toDto(List<VolunteerCertificate> volunteerCertificates);

    @AfterMapping
    default void updateDto(VolunteerCertificate certificate, @MappingTarget VolunteerCertificateDto certificateDto){
        certificateDto.setOrganizationName(certificate.getProject().getOrganization().getOrganizationName());
        certificateDto.setProjectTitle(certificate.getProject().getTitle());
    }

}
