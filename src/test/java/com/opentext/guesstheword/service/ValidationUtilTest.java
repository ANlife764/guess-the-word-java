package com.opentext.guesstheword.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationUtilTest {

    @ParameterizedTest
    @CsvSource({
            "abcd, false",
            "abcde, true",
            "ab1de, false",
            "AbCdEf, true",
            "'', false"
    })
    void usernameRules(String username, boolean expectedValid) {
        boolean valid = ValidationUtil.validateUsername(username) == null;
        assertThat(valid).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
            "'ab$1', false",
            "'abcde$', false",
            "'abcde1', false",
            "'12345$', false",
            "'abc1$', true",
            "'abc1%', true",
            "'abc1*', true"
    })
    void passwordRules(String password, boolean expectedValid) {
        boolean valid = ValidationUtil.validatePassword(password) == null;
        assertThat(valid).isEqualTo(expectedValid);
    }

    @ParameterizedTest
    @CsvSource({
            "TOWER, true",
            "tower, false",
            "ABC, false",
            "AB12E, false"
    })
    void guessMustBeFiveUppercaseLetters(String guess, boolean expectedValid) {
        assertThat(ValidationUtil.isWellFormedGuess(guess)).isEqualTo(expectedValid);
    }
}
