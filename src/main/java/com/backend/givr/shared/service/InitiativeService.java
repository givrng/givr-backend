package com.backend.givr.shared.service;

import com.backend.givr.organization.dtos.CheckoutResponse;
import com.backend.givr.organization.security.ProjectServiceWorker;
import com.backend.givr.redis.RedisService;
import com.backend.givr.shared.dtos.*;
import com.backend.givr.shared.entity.Creator;
import com.backend.givr.shared.entity.ProjectPaymentReceipt;
import com.backend.givr.shared.entity.projects.Media;
import com.backend.givr.shared.entity.projects.Resource;
import com.backend.givr.shared.entity.projects.Initiative;
import com.backend.givr.shared.entity.projects.InitiativeItem;
import com.backend.givr.shared.enums.ProjectStatus;
import com.backend.givr.shared.enums.ResourceKind;
import com.backend.givr.shared.enums.ReviewStatus;
import com.backend.givr.shared.exceptions.IllegalOperationException;
import com.backend.givr.shared.interfaces.SecurityDetails;
import com.backend.givr.shared.mapper.ProjectMapper;
import com.backend.givr.shared.repo.InitiativeItemRepo;
import com.backend.givr.shared.repo.MediaRepo;
import com.backend.givr.shared.repo.ResourceRepo;
import com.backend.givr.shared.repo.InitiativeRepo;
import com.backend.givr.volunteer.entity.Volunteer;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;


@Slf4j
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
    private final RedisService redisService;
    private final VerificationWorker verificationService;

    @Transactional
    public Initiative createInitiative(InitiativeRequestDto initiativeRequestDto, Volunteer individual){
        Initiative initiative = mapper.toInitiative(initiativeRequestDto);
        initiative.setOrganizerName(String.format("%s %s", individual.getLastname(), individual.getFirstname()));
        projectService.handleProject(initiative, initiativeRequestDto);


        MediaDto mediaDto = initiativeRequestDto.getMedia();
        Media media = new Media(mediaDto.mediaUrl());

        initiative.setMedia(media);
        initiative.setReviewStatus(ReviewStatus.Pending);

        Creator creator = creatorService.createOrReturnCreator(individual);
        initiative.setCreator(creator);
        initiative.setStatus(ProjectStatus.DRAFT);

        initiative = repo.save(initiative);

        if(initiativeRequestDto.getInitiativeItemList() != null){
            List<InitiativeItem> items = createInitiativeItems(initiativeRequestDto.getInitiativeItemList(), ResourceKind.ITEM, initiative);
            initiative.getInitiativeItems().addAll(items);
        }

        if(initiativeRequestDto.getInitiativeServiceList() != null){
            List<InitiativeItem> services = createInitiativeItems(initiativeRequestDto.getInitiativeServiceList(), ResourceKind.SERVICE, initiative);
            initiative.getInitiativeServices().addAll(services);
        }

        projectServiceWorker.createProjectCard(initiative);

        return  initiative;
    }
    @Transactional
    public CheckoutResponse initializePublishProject(PublishProjectDto publishProjectDto, SecurityDetails details) {
        Creator creator = creatorService.getCreatorByIndividualId(details.getId());
        Long projectId = publishProjectDto.initiativeId();
        Initiative initiative = getInitiative(projectId);

        ProjectPaymentReceipt receipt = initiative.getCreationReceipt();

        if(!initiative.getReviewStatus().equals(ReviewStatus.Approved))
            throw new IllegalOperationException("Admin must approve initiative before it can be published");

        if(creator.hasFreeProject()){
            publishProject(initiative, creator);
            log.info("Creator free project count {}", creator.getFreeProjectCount());
            creator.useFreeProject();
        }else {
            log.info("{}", receipt!=null && receipt.getStatus()!=null && !receipt.getStatus().equals(TransactionStatus.SUCCESSFUL));
            if(receipt!=null && receipt.getStatus()!=null && receipt.getStatus().equals(TransactionStatus.SUCCESSFUL))
                publishProject(initiative, creator);
            else
                return verificationService.createInitiativePayment(initiative, details, publishProjectDto);
        }
        return null;
    }

    public void publishProject(Initiative project, Creator creator) {

        if(project.getReviewStatus() != null && !project.getReviewStatus().equals(ReviewStatus.Approved))
            throw new IllegalOperationException("Project must be approved by admin to be published");

        if(!project.getCreator().equals(creator))
            throw new IllegalOperationException("Cannot publish a project that belongs to another creator");

        // Grants individual access to project in-app messages
        redisService.addAuthorizedUserProjects(creator.getIndividual().getVolunteerId(), project.getProjectId());
        project.setStatus(ProjectStatus.OPEN);

        try{
            save(project);
            projectServiceWorker.sendProjectListing(project).subscribe();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Initiative getInitiative(Long initiativeId, Volunteer individual){
        return repo.findByProjectIdAndCreatorIndividual(initiativeId, individual)
                .orElseThrow(()-> new EntityNotFoundException("Initiative does not exist"));
    }

    public Initiative getInitiative(Long initiativeId){
        return repo.findById(initiativeId).orElseThrow(()-> new EntityNotFoundException("Initiative does not exist"));
    }

    public Initiative getInitiativeByStatusNot(Long initiativeId, ProjectStatus status){
        return repo.findByProjectIdAndStatusNot(initiativeId, status).orElseThrow(()->new EntityNotFoundException("Initiative does not exist"));
    }

    public InitiativeResponseDto getInitiativeDto(Long initiativeId, SecurityDetails details){
        var initiative = getInitiative(initiativeId);
        ProjectStatus status = initiative.getStatus();

        if(status.equals(ProjectStatus.DRAFT) )
            if(initiative.getCreator().getIndividual().getVolunteerId().equals(details.getId()))
                return mapper.toInitiative(initiative);
            else
                throw new EntityNotFoundException("Initiative does not exist");

        return mapper.toInitiative(initiative);
    }

    public Initiative getInitiativeByIdAndCreator(Long initiativeId, Creator creator){
        return  repo.findByProjectIdAndCreator(initiativeId, creator).orElseThrow(()-> new EntityNotFoundException("Entity does not exist or belong to individual"));
    }

    public void updateInitiativeItems(List<InitiativeItem> items){
        itemRepo.saveAllAndFlush(items);
    }

    @Transactional
    public Initiative updateInitiative(Long initiativeId, Volunteer individual, InitiativeRequestDto requestDto){
        Initiative initiative = repo.findById(initiativeId).orElseThrow(()-> new EntityNotFoundException("Initiative does not exist"));

        if(!initiative.getCreator().getIndividual().equals(individual))
            throw new IllegalOperationException("Cannot update an initiative that is not own by account");

        if(!initiative.getStatus().equals(ProjectStatus.DRAFT))
            throw new IllegalOperationException("Only draft projects can be updated");

        if(requestDto.getInitiativeServiceList() != null && !requestDto.getInitiativeServiceList().isEmpty()){
            updateInitiativeResources(initiative.getInitiativeServices(), initiative, ResourceKind.SERVICE, requestDto.getInitiativeServiceList());
        }

        if(requestDto.getInitiativeItemList() != null && !requestDto.getInitiativeItemList().isEmpty())
            updateInitiativeResources(initiative.getInitiativeItems(),initiative, ResourceKind.ITEM, requestDto.getInitiativeItemList());

        if(requestDto.getMedia()!= null && requestDto.getMedia().mediaUrl() != null && !requestDto.getMedia().mediaUrl().isEmpty()){
            initiative.setMedia(new Media(requestDto.getMedia().mediaUrl()));
        }

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

        if(!initiative.getStatus().equals(ProjectStatus.DRAFT))
            throw new IllegalOperationException("Only drafts can be deleted");
        String deleteTitle = String.format("DEL-%s", LocalDateTime.now());
        String newTitle = String.format("%s-%s", initiative.getTitle(), deleteTitle);

        initiative.setTitle(newTitle);
        initiative.setDeleted(true);
        repo.save(initiative);
    }

    public InitiativeItem getInitiativeItem(String resourceId){
        return  itemRepo.findById(resourceId)
                .orElseThrow(()-> new EntityNotFoundException(String.format("Initiative item with resourceId: %s does not exist", resourceId)));
    }

    @Transactional
    private void updateInitiativeResources(List<InitiativeItem> resources,Initiative initiative, ResourceKind kind, List<InitiativeResourceDto> resourceDto){
        itemRepo.deleteAll(resources);
        resources.clear();
        List<InitiativeItem> items = createInitiativeItems(resourceDto, kind, initiative);
        resources.addAll(items);
    }

    private Resource createOrReturnResource(String resourceName){
        String name = resourceName.toLowerCase(Locale.ROOT).trim();
        return resourceRepo.findByName(name)
                .orElseGet(()->{
                    return resourceRepo.save(new Resource(name));
                });
    }

    private List<InitiativeItem> createInitiativeItems(List<InitiativeResourceDto> resources, ResourceKind kind, Initiative initiative){
        return resources.stream().map(resource-> createOrUpdateInitiativeItem(resource, kind, initiative)).toList();
    }

    private InitiativeItem createOrUpdateInitiativeItem(InitiativeResourceDto resourceDto, ResourceKind kind, Initiative initiative){
        var quantity = resourceDto.getQuantity();
        if(quantity < 1)
            throw new IllegalOperationException(String.format("Cannot request for less than one %s", resourceDto.getResource()));

        var resource = createOrReturnResource(resourceDto.getResource());

        AtomicReference<InitiativeItem> result = new AtomicReference<>();

        itemRepo.findByResourceAndInitiative(resource, initiative).ifPresentOrElse(item ->{
            item.setQuantityRequested(quantity);
            result.set(item);
        }, ()->{
            result.set(new InitiativeItem(resource, quantity, initiative, kind));
        });

        return result.get();
    }

//    @Scheduled(cron = "0 30 1 * * *", zone = "Africa/Lagos")
//    @Transactional
//    public void updateProjectStatusOnDeadline(){
//        LocalDate today = LocalDate.now(ZoneId.of("Africa/Lagos"));
//        List<Initiative> projects = repo.findExpiredInitiatives(today);
//        List<Initiative> reviewableProjects = repo.findClosingInitiatives(today.plusDays(1));
//        projects.forEach(project -> {
//            if(project.shouldClose(today.atStartOfDay()))
//                project.closeApplication();
//        });
//
//        reviewableProjects.forEach(project -> project.setReviewable(true));
//    }

    public Initiative findInitiativeById(Long projectId) {
        return repo.findById(projectId).orElseThrow(()->new EntityNotFoundException("Initiative does not exist"));
    }

    public void save(Initiative project) {
        repo.save(project);
    }

    public InitiativeResourceResponseDto getInitiativeResources(Long initiativeId) {
        Initiative initiative = getInitiativeByStatusNot(initiativeId, ProjectStatus.DRAFT);

        Media media = initiative.getMedia();
        List<InitiativeResourceDto> items = mapper.toResourcesDto(initiative.getInitiativeItems());

        List<InitiativeResourceDto> services = mapper.toResourcesDto(initiative.getInitiativeServices());

        return new InitiativeResourceResponseDto(
                new MediaDto(media.getMediaType(), media.getUrl()),
                items,
                services
        );
    }

    public int getIndividualInitiativeCount(Creator creator) {
        return repo.countByCreatorAndStatusNot(creator, ProjectStatus.DRAFT);
    }

    public PagedModel<InitiativeResponseDto> getAllInitiativeByStatus(ProjectStatus projectStatus, PageRequest pageRequest) {
        pageRequest = pageRequest.withSort(Sort.by("createdAt").descending());
        Page<Initiative> initiativePage = switch (projectStatus){
            case ALL -> repo.findAll(pageRequest);
            case COMPLETED, OPEN, CLOSE, ONGOING, DRAFT -> repo.findAllByStatus(projectStatus, pageRequest);
        };

        PageImpl<InitiativeResponseDto> initiativeResponseDtos = new PageImpl<>(
                mapper.toInitiativeReponses(initiativePage.getContent()),
                initiativePage.getPageable(),
                initiativePage.getTotalElements()
        );

        return new PagedModel<>(initiativeResponseDtos);
    }


}
