package com.backend.givr.shared.service;

import com.backend.givr.shared.TokenId;
import com.backend.givr.shared.repo.TokenIdRepo;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TokenIdService {
    @Autowired
    private TokenIdRepo repo;

    public void createToken(String tokenId, String email, long expiration){
        TokenId token = new TokenId(email, tokenId);
        repo.save(token);
    }

    public boolean isTokenValid(String tokenId){
        TokenId token = repo.findById(tokenId).orElseThrow(()-> new EntityNotFoundException("Token does not exist"));
        if(!token.isUsed() && !token.isRevoked()){
            token.setUsed(true);
            repo.save(token);
            return true;
        }
        revokeTokens(token.getEmail());
        return false;
    }
    public void revokeTokens(String email){
        List<TokenId> tokens = repo.findAllByEmailAndIsUsed(email, false);
        tokens.forEach(TokenId::revoke);
        repo.saveAll(tokens);
    }
}

