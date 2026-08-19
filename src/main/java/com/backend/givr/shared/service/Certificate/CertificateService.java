package com.backend.givr.shared.service.Certificate;

import com.backend.givr.admin.dtos.BatchCertificateRequest;
import com.backend.givr.organization.entity.Participation;
import com.backend.givr.organization.repo.ParticipationRepo;
import com.backend.givr.shared.dtos.RenderCertificateDto;
import com.backend.givr.shared.dtos.VolunteerCertificateDto;
import com.backend.givr.shared.dtos.VolunteerCertificateVerificationDTO;
import com.backend.givr.shared.entity.VolunteerCertificate;
import com.backend.givr.shared.enums.CertificationStatus;
import com.backend.givr.shared.mapper.CertificateMapper;
import com.backend.givr.shared.repo.VolunteerCertificateRepo;
import com.backend.givr.shared.email.EmailService;
import com.backend.givr.shared.service.CloudinaryService;
import com.backend.givr.shared.service.GivrImageRendererService;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Flux;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
@Slf4j
public class CertificateService {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private VolunteerCertificateRepo repo;
    @Autowired
    private EmailService emailService;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private GivrImageRendererService imageRendererService;
    @Autowired
    private ParticipationRepo participationRepo;
    @Autowired
    private CertificateMapper certMapper;
    @Autowired
    private ObjectMapper mapper;

    @Transactional
    @Async
    public void generateCertificate(Long participantId){
        Participation participant = participationRepo.findById(participantId).orElseThrow(()->new EntityNotFoundException("Cannot issue a certificate to a participant that does not exist"));
        if(participant.getCertificationStatus()== CertificationStatus.Certified)
            return;

        try{
            VolunteerCertificate certificate = new VolunteerCertificate(participant);
            RenderCertificateDto certificateDto = new RenderCertificateDto(participant.getProject(),
                    participant.getVolunteer(), certificate.getCertId());

            byte[] certificateImg = imageRendererService.renderCertificate(certificateDto);

            String shareableLink = cloudinaryService.uploadImage(certificateImg, "certificates", certificate.getCertId());
            certificate.setCertUrl(shareableLink);
            repo.save(certificate);
            participant.setCertificationStatus(CertificationStatus.Certified);
            emailService.sendCertificateReadyNotification(participant.getVolunteer(), participant.getProject());
            log.info("Certificate certId:{} has been successfully issued to participant {}", certificate.getCertId(),participantId);
        }catch (RuntimeException e){
            log.error("Failed to issue certificate to participant: {} because {}", participant.getId(), e.getLocalizedMessage());
        }
    }

    public VolunteerCertificateVerificationDTO verifyCertificate(String certId){
        VolunteerCertificate certificate = repo.findById(certId)
                .orElseThrow(()->new EntityNotFoundException("Certificate does not exist"));
        Volunteer volunteer = certificate.getCertifiedVolunteer();

        VolunteerCertificateVerificationDTO certificateVerificationDTO = new VolunteerCertificateVerificationDTO();
        certificateVerificationDTO.setCertificate(certMapper.toDto(certificate));
        certificateVerificationDTO.setVolunteerFirstName(volunteer.getFirstname());
        certificateVerificationDTO.setVolunteerMiddleName(volunteer.getMiddleName());
        certificateVerificationDTO.setVolunteerLastName(volunteer.getLastname());

        return certificateVerificationDTO;
    }
}
