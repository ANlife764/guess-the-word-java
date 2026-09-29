package com.opentext.guesstheword.model;

public enum Role {
    ADMIN, PLAYER;

    /** Lower-case form used in the session, templates and JSON, e.g. "admin". */
    public String label() {
        return name().toLowerCase();
    }
}
