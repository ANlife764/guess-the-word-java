package com.opentext.guesstheword.model;

public enum GameStatus {
    ACTIVE, WON, LOST;

    public String label() {
        return name().toLowerCase();
    }
}
