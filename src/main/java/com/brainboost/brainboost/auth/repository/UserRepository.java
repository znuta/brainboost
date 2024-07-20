package com.brainboost.brainboost.auth.repository;

import com.brainboost.brainboost.auth.entity.AppUser;
import com.brainboost.brainboost.auth.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByUserName(String userName);

    AppUser findUserByUserName(String username);

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByFirstNameOrLastNameOrEmail(String firstName, String lastName, String email);

    List<AppUser> findByFirstNameOrLastName(String firstname, String lastname);

    Optional<AppUser> findByRole(Roles role);
}
