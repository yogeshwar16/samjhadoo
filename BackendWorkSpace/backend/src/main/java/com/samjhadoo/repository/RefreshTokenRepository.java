package com.samjhadoo.repository;

import com.samjhadoo.model.RefreshToken;
import com.samjhadoo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    
    Optional<RefreshToken> findByToken(String token);
    
    @Modifying
    int deleteByUser(User user);
    
    @Modifying
    int deleteByToken(String token);
    
    boolean existsByTokenAndRevokedFalse(String token);
}
