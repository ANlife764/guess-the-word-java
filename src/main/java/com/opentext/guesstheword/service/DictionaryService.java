package com.opentext.guesstheword.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Holds the set of accepted 5-letter English words, loaded once at startup. */
@Service
public class DictionaryService {

    private final Set<String> validWords;

    public DictionaryService() throws IOException {
        Set<String> words = new HashSet<>();
        ClassPathResource resource = new ClassPathResource("data/valid_words.txt");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String w = line.trim().toUpperCase();
                if (!w.isEmpty()) {
                    words.add(w);
                }
            }
        }
        this.validWords = Collections.unmodifiableSet(words);
    }

    public boolean isValidWord(String word) {
        return validWords.contains(word);
    }

    public int size() {
        return validWords.size();
    }
}
