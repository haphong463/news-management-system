package com.windev.user_service.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.javaguides.common_lib.entity.AbstractEntity;

@Entity
@Table(name = "user_passwordresetrequest")
@Getter
@Setter
@Builder
public class UserPasswordResetRequest extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String verificationCode;

    private Date verificationCodeExpiration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
