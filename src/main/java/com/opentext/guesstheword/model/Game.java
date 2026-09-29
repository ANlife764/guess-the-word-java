package com.opentext.guesstheword.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "game")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "word_id")
    private Word word;

    @Column(nullable = false)
    private LocalDate playDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status = GameStatus.ACTIVE;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Guess> guesses = new ArrayList<>();

    protected Game() {
    }

    public Game(AppUser user, Word word, LocalDate playDate) {
        this.user = user;
        this.word = word;
        this.playDate = playDate;
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Word getWord() {
        return word;
    }

    /** Mutable on purpose (standard JPA entity practice) - also lets tests pin a known word. */
    public void setWord(Word word) {
        this.word = word;
    }

    public LocalDate getPlayDate() {
        return playDate;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public List<Guess> getGuesses() {
        return guesses;
    }
}
