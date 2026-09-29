package com.opentext.guesstheword.repository;

import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameLower(String usernameLower);
    List<AppUser> findByRoleOrderByUsernameAsc(Role role);
    boolean existsByRole(Role role);
}
