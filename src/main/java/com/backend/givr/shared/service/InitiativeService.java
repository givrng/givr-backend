package com.backend.givr.shared.service;

import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.shared.dtos.InitiativeRequestDto;
import com.backend.givr.shared.dtos.InitiativeResourceDto;
import com.backend.givr.shared.dtos.InitiativeResourceResponseDto;
import com.backend.givr.shared.dtos.MediaDto;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.projects.Media;
import com.backend.givr.shared.entity.projects.Resource;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.projects.InitiativeItem;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.repo.InitiativeItemRepo;
import com.backend.givr.shared.repo.MediaRepo;
import com.backend.givr.shared.repo.ResourceRepo;
import com.backend.givr.shared.repo.InitiativeRepo;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class InitiativeService {
    private final InitiativeRepo repo;
    private final ProjectService projectService;
    private final CreatorService creatorService;
    private final ProjectServiceWorker projectServiceWorker;
    private final ProjectMapper mapper;
    private final ResourceRepo resourceRepo;
    private final InitiativeItemRepo itemRepo;
    private final MediaRepo mediaRepo;

    @Transactional
    public Initiative createInitiative(InitiativeRequestDto initiativeRequestDto, Volunteer individual){
        Initiative initiative = mapper.toInitiative(initiativeRequestDto);
        initiative.setOrganizerName(String.format("%s %s", individual.getLastname(), individual.getFirstname()));
        projectService.handleProject(initiative, initiativeRequestDto);

        List<InitiativeItem> items = createInitiativeItems(initiativeRequestDto.getInitiativeItemList(), initiative);
        List<InitiativeItem> services = createInitiativeItems(initiativeRequestDto.getInitiativeItemList(), initiative);

        MediaDto mediaDto = initiativeRequestDto.getMedia();
        Media media = new Media(mediaDto.mediaUrl());
        media = mediaRepo.save(media);

        initiative.getInitiativeItems().addAll(items);
        initiative.getInitiativeServices().addAll(services);

        initiative.setMedia(media);


        Creator creator = creatorService.createOrReturnCreator(individual);
        initiative.setCreator(creator);
        initiative.setStatus(ProjectStatus.DRAFT);

        initiative = repo.save(initiative);
        projectServiceWorker.createProjectCard(initiative);

        return  initiative;
    }

    public Initiative getInitiative(Long initiativeId, Volunteer individual){
        return repo.findByProjectIdAndCreatorIndividual(initiativeId, individual)
                .orElseThrow(()-> new EntityNotFoundException("Initiative does not exist"));
    }

    public Initiative getInitiative(Long initiativeId){
        return repo.findById(initiativeId).orElseThrow(()-> new EntityNotFoundException("Initiative does not exist"));
    }

    public void updateInitiativeItems(List<InitiativeItem> items){
        itemRepo.saveAll(items);
    }

    public Initiative updateInitiative(Long initiativeId, Volunteer individual, InitiativeRequestDto requestDto){
        Initiative initiative = repo.findById(initiativeId).orElseThrow(()-> new EntityNotFoundException("Initiative does not exist"));

        if(!initiative.getCreator().getIndividual().equals(individual))
            throw new IllegalOperationException("Cannot update an initiative that is not own by account");

        if(requestDto.getInitiativeServiceList() != null && !requestDto.getInitiativeItemList().isEmpty()){
            updateInitiativeResources(initiative.getInitiativeServices(), requestDto);
        }

        if(requestDto.getInitiativeItemList() != null && !requestDto.getInitiativeItemList().isEmpty())
            updateInitiativeResources(initiative.getInitiativeItems(), requestDto);

        projectService.updateProject(initiative, requestDto);
        return initiative;
    }

    public List<Initiative> getAllInitiativeByIndividualAndStatus(Creator creator, ProjectStatus status){
        return repo.findAllByCreatorAndStatus(creator, status);
    }

    public Page<Initiative> getAllInitiativeByIndividualAndStatus(Volunteer individual, ProjectStatus status, PageRequest pageRequest){
        Creator creator = creatorService.createOrReturnCreator(individual);
        return status.equals(ProjectStatus.ALL)? repo.findAllByCreator(creator, pageRequest) : repo.findAllByCreatorAndStatus(creator, status, pageRequest);
    }

    public void deleteInitiative(Long initiativeId, Volunteer individual){
        Creator creator = creatorService.createOrReturnCreator(individual);
        Initiative initiative = repo.findByProjectIdAndCreator(initiativeId, creator)
                .orElseThrow(()-> new EntityNotFoundException("Initiative not found"));

        switch (initiative.getStatus()){
            case DRAFT -> {
                repo.delete(initiative);
            }
            case OPEN -> {
                initiative.setDeleted(true);
                repo.save(initiative);
            }
            default -> throw new IllegalOperationException("Only DRAFT or OPEN initiatives can be deleted");
        }
    }

    public InitiativeItem getInitiativeItem(String resourceId){
        return  itemRepo.findById(resourceId)
                .orElseThrow(()-> new EntityNotFoundException(String.format("Initiative item with resourceId: %s does not exist", resourceId)));
    }

    private void updateInitiativeResources(List<InitiativeItem> resources, InitiativeRequestDto requestDto){
        Initiative initiative = resources.getFirst().getInitiative();
        resources.clear();
        List<InitiativeItem> items = createInitiativeItems(requestDto.getInitiativeServiceList(), initiative);
        resources.addAll(items);
    }

    private Resource createOrReturnResource(String resourceName){
        String name = resourceName.toLowerCase(Locale.ROOT).trim();
        return resourceRepo.findByName(name)
                .orElseGet(()->{
                    return resourceRepo.save(new Resource(name));
                });
    }

    private List<InitiativeItem> createInitiativeItems(List<InitiativeResourceDto> resources, Initiative initiative){
        return resources.stream().map(service->{
            var resource = createOrReturnResource(service.resource());
            var quantity = service.quantity();

            return new InitiativeItem(resource, quantity, initiative);
        }).toList();
    }

    @Scheduled(cron = "0 30 1 * * *", zone = "Africa/Lagos")
    @Transactional
    public void updateProjectStatusOnDeadline(){
        LocalDate today = LocalDate.now(ZoneId.of("Africa/Lagos"));
        List<Initiative> projects = repo.findExpiredInitiatives(today);
        List<Initiative> reviewableProjects = repo.findClosingInitiatives(today.plusDays(1));
        projects.forEach(project -> {
            if(project.shouldClose(today.atStartOfDay()))
                project.closeApplication();
        });

        reviewableProjects.forEach(project -> project.setReviewable(true));
    }

    public Initiative findInitiativeById(Long projectId) {
        return repo.findById(projectId).orElseThrow(()->new EntityNotFoundException("Initiative does not exist"));
    }

    public void save(Initiative project) {
        repo.save(project);
    }

    public InitiativeResourceResponseDto getInitiativeResources(Long initiativeId) {
        Initiative initiative = getInitiative(initiativeId);
        Media media = initiative.getMedia();
        List<InitiativeResourceDto> items = initiative.getInitiativeItems().stream()
                .map(item-> new InitiativeResourceDto(item.getResource().getName(), item.getResource().getId(), item.getQuantityRequested())).toList();

        List<InitiativeResourceDto> services = initiative.getInitiativeServices().stream()
                .map(item-> new InitiativeResourceDto(item.getResource().getName(), item.getResource().getId(), item.getQuantityRequested())).toList();

        return new InitiativeResourceResponseDto(
                new MediaDto(media.getMediaType(), media.getUrl()),
                items,
                services
        );
    }
}
