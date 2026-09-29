package com.opentext.guesstheword.config;

import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Role;
import com.opentext.guesstheword.model.Word;
import com.opentext.guesstheword.repository.AppUserRepository;
import com.opentext.guesstheword.repository.WordRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    /** Same 20 seed words as the Python version, so both submissions start identically. */
    private static final List<String> WORDS = List.of(
            "TOWER", "HOUSE", "PLANT", "RIVER", "CLOUD", "BREAD", "STONE", "LIGHT", "MUSIC", "TABLE",
            "CHAIR", "WATER", "EARTH", "FLAME", "GRASS", "OCEAN", "SMILE", "DREAM", "BEACH", "TRAIN");

    private final WordRepository wordRepository;
    private final AppUserRepository userRepository;

    public DataSeeder(WordRepository wordRepository, AppUserRepository userRepository) {
        this.wordRepository = wordRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (wordRepository.count() == 0) {
            WORDS.forEach(w -> wordRepository.save(new Word(w)));
        }
        if (!userRepository.existsByRole(Role.ADMIN)) {
            String hash = new BCryptPasswordEncoder().encode("Admin$123");
            userRepository.save(new AppUser("admin", hash, Role.ADMIN));
        }
    }
}
