package com.windev.reaction_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "reactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long articleId;

    private Long commentId;

    @ManyToOne
    @JoinColumn(name = "reaction_type_id", nullable = false)
    private ReactionType reactionType;

    private Date createdAt;
}