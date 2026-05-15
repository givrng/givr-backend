package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.GivrMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GivrMesssageRepo extends JpaRepository<GivrMessage, String> {
  Optional<GivrMessage> findFirstBy(Sort sort);
}
