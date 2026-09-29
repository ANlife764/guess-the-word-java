package com.opentext.guesstheword.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/** JSON shapes for the game API. Field names are snake_case to match static/game.js unchanged. */
public final class GameDtos {

    private GameDtos() {
    }

    public record GuessDto(String letters, String[] result) {
    }

    public record ActiveGameDto(String status, List<GuessDto> guesses) {
    }

    public record StateDto(
            ActiveGameDto game,
            @JsonProperty("games_today") long gamesToday,
            @JsonProperty("max_games") int maxGames,
            @JsonProperty("max_guesses") int maxGuesses) {
    }

    public record GuessResultDto(String status, List<GuessDto> guesses, String word) {
    }

    public record ErrorDto(String error) {
    }

    public record GuessRequest(String guess) {
    }
}
