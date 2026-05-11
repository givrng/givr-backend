package com.backend.givr.shared.mapper;

import com.backend.givr.organization.dtos.OrganizationUpdateDto;
import com.backend.givr.shared.entity.OrganizationVerificationSession;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VerificationMapper {

    @Mapping(source = "cacRegNumber", target = "claimedCACRegNumber")
    @Mapping(source = "name", target = "claimedOrgName")
    OrganizationVerificationSession toVerificationSession(OrganizationUpdateDto updateDto);

    @Mapping(source = "name", target = "claimedOrgName")
    @Mapping(source = "cacRegNumber", target = "claimedCACRegNumber")
    @Mapping(source = "address", target = "claimedAddress")
    void updateVerificationSession(OrganizationUpdateDto updateDto, @MappingTarget OrganizationVerificationSession verificationSession);

    @AfterMapping
    default void updateVerificationSess(OrganizationUpdateDto updateDto, @MappingTarget OrganizationVerificationSession verificationSession){
        verificationSession.setIdNumber(null);
        if(updateDto.getContactVerification().idNumber() !=null) {
            verificationSession.setIdNumber(updateDto.getContactVerification().idNumber().trim().toUpperCase());
        }
        verificationSession.setIdType(updateDto.getContactVerification().idType());
        verificationSession.setUsrImgUrl(updateDto.getContactVerification().usrImgUrl());
        verificationSession.setClaimedType(updateDto.getCategory().getFirst());

    }
}
