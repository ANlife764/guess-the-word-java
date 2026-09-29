package com.opentext.guesstheword.repository;

import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Game;
import com.opentext.guesstheword.model.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    long countByUserAndPlayDate(AppUser user, LocalDate playDate);

    Optional<Game> findFirstByUserAndStatus(AppUser user, GameStatus status);

    List<Game> findByPlayDate(LocalDate playDate);

    List<Game> findByUserOrderByPlayDateAsc(AppUser user);

    long countByPlayDateAndStatus(LocalDate playDate, GameStatus status);

    @Query("select count(distinct g.user) from Game g where g.playDate = :day")
    long countDistinctUsersOnDate(@Param("day") LocalDate day);
}
