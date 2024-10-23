package com.windev.reaction_service.repository;

import com.windev.reaction_service.entity.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionTypeRepository extends JpaRepository<ReactionType, Long> {

    Optional<ReactionType> findByType(String type);
}
