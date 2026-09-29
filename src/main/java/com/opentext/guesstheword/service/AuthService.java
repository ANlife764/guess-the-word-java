package com.opentext.guesstheword.service;

import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Role;
import com.opentext.guesstheword.repository.AppUserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new player. Checks for an existing (case-insensitive) username up front,
     * and still falls back to catching the database's unique-constraint error - guarding
     * against two near-simultaneous submissions of the same username (e.g. a double click).
     */
    @Transactional
    public void register(String username, String password) {
        String lower = username.toLowerCase();
        if (userRepository.findByUsernameLower(lower).isPresent()) {
            throw new GameException("'" + username + "' is already registered (usernames are "
                    + "not case-sensitive). Try logging in, or pick a different username.");
        }
        try {
            AppUser user = new AppUser(username, passwordEncoder.encode(password), Role.PLAYER);
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new GameException("'" + username + "' is already registered. Try logging in, "
                    + "or pick a different username.");
        }
    }

    @Transactional(readOnly = true)
    public Optional<AppUser> authenticate(String username, String password) {
        return userRepository.findByUsernameLower(username == null ? "" : username.toLowerCase())
                .filter(u -> passwordEncoder.matches(password == null ? "" : password, u.getPasswordHash()));
    }
}
