package com.opentext.guesstheword.service;

import java.util.HashMap;
import java.util.Map;

/** Pure Wordle-style scoring: G = right letter/right spot, O = right letter/wrong spot, X = not in word. */
public final class ScoreUtil {

    private ScoreUtil() {
    }

    public static String[] score(String guess, String word) {
        String[] result = {"X", "X", "X", "X", "X"};
        Map<Character, Integer> remaining = new HashMap<>();

        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == word.charAt(i)) {
                result[i] = "G";
            } else {
                char c = word.charAt(i);
                remaining.merge(c, 1, Integer::sum);
            }
        }
        for (int i = 0; i < 5; i++) {
            if (!result[i].equals("G")) {
                char c = guess.charAt(i);
                Integer left = remaining.get(c);
                if (left != null && left > 0) {
                    result[i] = "O";
                    remaining.put(c, left - 1);
                }
            }
        }
        return result;
    }
}
