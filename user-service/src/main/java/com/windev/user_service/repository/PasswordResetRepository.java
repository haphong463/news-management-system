package com.windev.user_service.repository;

import com.windev.user_service.entity.UserPasswordResetRequest;
import com.windev.user_service.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetRepository extends JpaRepository<UserPasswordResetRequest, Long> {
    Optional<UserPasswordResetRequest> findByUser(User user);
    Optional<UserPasswordResetRequest> findByVerificationCode(String code);
}
