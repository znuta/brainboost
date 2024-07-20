package com.brainboost.brainboost.auth.repository;
import com.brainboost.brainboost.auth.entity.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolesRepository extends JpaRepository<Roles,Long> {
    Optional<Roles> findByName(String name);

    Page<Roles> findUsingPattern(String pattern, Pageable pageable);

}
