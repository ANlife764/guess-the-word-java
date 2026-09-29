package com.opentext.guesstheword.service;

import org.springframework.http.HttpStatus;

/** Thrown for any expected, user-facing failure (bad guess, no active game, daily limit, etc). */
public class GameException extends RuntimeException {

    private final HttpStatus status;

    public GameException(String message) {
        this(message, HttpStatus.BAD_REQUEST);
    }

    public GameException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
