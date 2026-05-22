package com.backend.givr.shared.service;

import com.backend.givr.shared.entity.GivrMessage;
import com.backend.givr.shared.entity.GivrUserProjectPointer;
import com.backend.givr.shared.repo.GivrMesssageRepo;
import com.backend.givr.shared.repo.GivrUserProjectPointerRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class GivrMessageService {

    @Autowired
    private GivrMesssageRepo repo;
    @Autowired
    private GivrUserProjectPointerRepo pointerRepo;

    public Page<GivrMessage> loadConversation(Long projectId, String cursor, int size){
        if(cursor.isEmpty()){
            Pageable pageable = PageRequest.of(0, size, Sort.by("sentAt").descending());
            return repo.findByProjectId(projectId, pageable);
        }

        Pageable pageable = PageRequest.of(0, size, Sort.by("sentAt").descending());
        return repo.findAllByProjectIdAndMsgIdLessThan(projectId, cursor, pageable);
    }


    public GivrUserProjectPointer getLastOffsetValue(){
        return pointerRepo.findFirstBy(Sort.by(Sort.Order.desc("createdAt"))).orElse(null);
    }

    public GivrMessage getMsgOffset() {
        return repo.findFirstBy(Sort.by(Sort.Order.desc("savedAt"))).orElse(null);
    }

    private void saveProjectPointer (GivrUserProjectPointer pointer){
        Optional<GivrUserProjectPointer> projectPointer = pointerRepo.findById(pointer.getUserId());

        projectPointer.ifPresentOrElse(p->{
            p.getProjectOffsets().putAll(pointer.getProjectOffsets());
            pointerRepo.save(p);
        }, ()->pointerRepo.save(pointer));
    }

    public List<GivrUserProjectPointer> saveAllProjectPointer(List<GivrUserProjectPointer> pointers){
        if(pointers.isEmpty())
            return null;

        pointers.forEach(this::saveProjectPointer);
        return pointers;
    }

    public List<GivrMessage> saveAllMessage(Iterable<GivrMessage> messages){
        return repo.saveAll(messages);
    }

    public long getUnreadCount(String offset){
        return repo.countByMsgIdGreaterThan(offset);
    }

    public Map<Long, Long > computeUserChatUnreadCount(String userId){
        Map<Long, Long> result = new HashMap<>();
        GivrUserProjectPointer userProjectPointer = pointerRepo.findById(userId).orElseThrow();

        Flux.fromIterable(userProjectPointer.getProjectOffsets().entrySet())
                .doOnNext(entry->{
                    if(entry == null)
                        return;
                    if(entry.getKey() != null && entry.getValue() != null)
                        result.put(entry.getKey(), this.getUnreadCount(entry.getValue()));
                }).subscribe();

        return result;
    }

}
