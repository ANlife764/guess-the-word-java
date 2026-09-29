package com.opentext.guesstheword.service;

import java.util.regex.Pattern;

/** Pure validation rules, kept separate from the web layer so they're easy to unit test. */
public final class ValidationUtil {

    private static final Pattern USERNAME = Pattern.compile("[A-Za-z]{5,}");
    private static final Pattern HAS_LETTER = Pattern.compile(".*[A-Za-z].*");
    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_SPECIAL = Pattern.compile(".*[$%*].*");
    private static final Pattern GUESS = Pattern.compile("[A-Z]{5}");

    private ValidationUtil() {
    }

    /** Returns an error message, or null if the username is valid. */
    public static String validateUsername(String u) {
        if (u == null || !USERNAME.matcher(u).matches()) {
            return "Username must be at least 5 letters (A-Z, a-z only).";
        }
        return null;
    }

    /** Returns an error message, or null if the password is valid. */
    public static String validatePassword(String p) {
        if (p == null || p.length() < 5 || !HAS_LETTER.matcher(p).matches()
                || !HAS_DIGIT.matcher(p).matches() || !HAS_SPECIAL.matcher(p).matches()) {
            return "Password must be at least 5 characters with a letter, a digit and one of $ % *.";
        }
        return null;
    }

    /** True if the guess is exactly 5 upper-case letters A-Z. */
    public static boolean isWellFormedGuess(String g) {
        return g != null && GUESS.matcher(g).matches();
    }
}
