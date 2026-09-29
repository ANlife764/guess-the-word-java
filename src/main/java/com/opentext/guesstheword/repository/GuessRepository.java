package com.opentext.guesstheword.repository;

import com.opentext.guesstheword.model.Game;
import com.opentext.guesstheword.model.Guess;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GuessRepository extends JpaRepository<Guess, Long> {
    List<Guess> findByGameOrderByIdAsc(Game game);
    long countByGame(Game game);
}
