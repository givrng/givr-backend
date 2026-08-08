package com.backend.givr.shared.service.Certificate;

import com.backend.givr.admin.dtos.BatchCertificateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class CertificateIssuingService {
    @Autowired
    private CertificateService service;

    @Async
    public void issueBatchCertificates(BatchCertificateRequest request){

        for (Long participantId: request.participants()){
            service.generateCertificate(participantId);
            try{
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    @Async
    public void issueSingleCertificateTo(Long participationId ){
        service.generateCertificate(participationId);
    }
}
