package com.opentext.guesstheword.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "guess")
public class Guess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "game_id")
    private Game game;

    @Column(nullable = false, length = 5)
    private String text;

    @Column(nullable = false)
    private Instant guessedAt = Instant.now();

    protected Guess() {
    }

    public Guess(Game game, String text) {
        this.game = game;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public Game getGame() {
        return game;
    }

    public String getText() {
        return text;
    }
}
