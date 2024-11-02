package com.windev.user_service.repository;

import com.windev.user_service.entity.UserEmailVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserEmailVerificationRepository extends JpaRepository<UserEmailVerification, Long> {
    Optional<UserEmailVerification> findByVerificationCode(String code);
}
