package com.backend.givr.shared.repo;

import com.backend.givr.shared.entity.TokenId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TokenIdRepo extends JpaRepository<TokenId, String> {
    List<TokenId> findAllByEmailAndIsUsed(String email, boolean isUsed);
}
