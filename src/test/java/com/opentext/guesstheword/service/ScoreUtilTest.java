package com.opentext.guesstheword.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoreUtilTest {

    @Test
    void exactAndMisplacedLetters() {
        assertThat(ScoreUtil.score("HOMER", "TOWER")).containsExactly("X", "G", "X", "G", "G");
    }

    @Test
    void singleMisplacedLetter() {
        assertThat(ScoreUtil.score("AUDIO", "TOWER")).containsExactly("X", "X", "X", "X", "O");
    }

    @Test
    void duplicateLettersAreNotOverCounted() {
        // ABIDE has one 'E'; SPEED's second 'E' (index 3) has no letter left to match against.
        assertThat(ScoreUtil.score("SPEED", "ABIDE")).containsExactly("X", "X", "O", "X", "O");
    }

    @Test
    void exactMatchIsAllGreen() {
        assertThat(ScoreUtil.score("TOWER", "TOWER")).containsExactly("G", "G", "G", "G", "G");
    }

    @Test
    void noLettersInCommonIsAllGrey() {
        assertThat(ScoreUtil.score("PLUMB", "CHAIR")).containsExactly("X", "X", "X", "X", "X");
    }
}
