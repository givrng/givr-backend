package com.backend.givr.organization.service.verify;

import com.backend.givr.organization.entity.CACResponse;
import lombok.Getter;

@Getter
public class CACRecord {
    private final String registrationNumber;
    private final String registeredName;

    private final Address registeredAddress;

    public CACRecord(String regNum, String name, Address address) {
        this.registrationNumber = regNum;
        this.registeredName = name;
        this.registeredAddress = address;
    }

    public CACRecord(CACResponse cacResponse){
        this.registrationNumber = cacResponse.getCacInfo().getRcNumber();
        this.registeredName = cacResponse.getCacInfo().getCompanyName();
        this.registeredAddress = cacResponse.getCacInfo().getBranchAddress();
    }
}
