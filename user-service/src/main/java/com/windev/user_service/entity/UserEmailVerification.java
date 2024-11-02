package com.windev.user_service.entity;

import jakarta.persistence.*;
import java.util.Date;
import lombok.*;
import net.javaguides.common_lib.entity.AbstractEntity;

@Entity
@Table(name = "user_emailverification")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailVerification extends AbstractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date verifiedAt;

    private String verificationCode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;


}
