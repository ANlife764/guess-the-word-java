package com.opentext.guesstheword.repository;

import com.opentext.guesstheword.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
    boolean existsByText(String text);
}
