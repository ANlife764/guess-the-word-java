package com.opentext.guesstheword.service;

import com.opentext.guesstheword.dto.GameDtos.*;
import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Game;
import com.opentext.guesstheword.model.GameStatus;
import com.opentext.guesstheword.model.Guess;
import com.opentext.guesstheword.model.Word;
import com.opentext.guesstheword.repository.GameRepository;
import com.opentext.guesstheword.repository.GuessRepository;
import com.opentext.guesstheword.repository.WordRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class GameService {

    public static final int MAX_GUESSES = 5;
    public static final int MAX_GAMES_PER_DAY = 3;

    private final GameRepository gameRepository;
    private final GuessRepository guessRepository;
    private final WordRepository wordRepository;
    private final DictionaryService dictionaryService;
    private final Random random = new Random();

    public GameService(GameRepository gameRepository, GuessRepository guessRepository,
                        WordRepository wordRepository, DictionaryService dictionaryService) {
        this.gameRepository = gameRepository;
        this.guessRepository = guessRepository;
        this.wordRepository = wordRepository;
        this.dictionaryService = dictionaryService;
    }

    @Transactional(readOnly = true)
    public StateDto getState(AppUser user) {
        Optional<Game> active = gameRepository.findFirstByUserAndStatus(user, GameStatus.ACTIVE);
        long playedToday = gameRepository.countByUserAndPlayDate(user, LocalDate.now());
        ActiveGameDto activeDto = active.map(this::toActiveGameDto).orElse(null);
        return new StateDto(activeDto, playedToday, MAX_GAMES_PER_DAY, MAX_GUESSES);
    }

    @Transactional
    public StateDto startGame(AppUser user) {
        Optional<Game> active = gameRepository.findFirstByUserAndStatus(user, GameStatus.ACTIVE);
        if (active.isPresent()) {
            return getState(user); // already have a game in progress; just return it
        }
        long playedToday = gameRepository.countByUserAndPlayDate(user, LocalDate.now());
        if (playedToday >= MAX_GAMES_PER_DAY) {
            throw new GameException("Daily limit of " + MAX_GAMES_PER_DAY
                    + " words reached. Come back tomorrow!");
        }
        List<Word> words = wordRepository.findAll();
        if (words.isEmpty()) {
            throw new GameException("No words configured.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        Word word = words.get(random.nextInt(words.size()));
        Game game = new Game(user, word, LocalDate.now());
        gameRepository.save(game);
        return getState(user);
    }

    @Transactional
    public GuessResultDto submitGuess(AppUser user, String rawGuess) {
        Game game = gameRepository.findFirstByUserAndStatus(user, GameStatus.ACTIVE)
                .orElseThrow(() -> new GameException("No active game. Start a new word."));

        String guess = rawGuess == null ? "" : rawGuess.trim();
        if (!ValidationUtil.isWellFormedGuess(guess)) {
            throw new GameException("Enter exactly 5 letters (A-Z, upper case).");
        }
        if (!dictionaryService.isValidWord(guess)) {
            throw new GameException(guess + " is not in the word list. Try another word.");
        }

        guessRepository.save(new Guess(game, guess));
        long count = guessRepository.countByGame(game);
        String word = game.getWord().getText();

        GameStatus status;
        if (guess.equals(word)) {
            status = GameStatus.WON;
        } else if (count >= MAX_GUESSES) {
            status = GameStatus.LOST;
        } else {
            status = GameStatus.ACTIVE;
        }
        game.setStatus(status);
        gameRepository.save(game);

        List<GuessDto> guesses = toGuessDtos(game);
        String revealedWord = status == GameStatus.ACTIVE ? null : word;
        return new GuessResultDto(status.label(), guesses, revealedWord);
    }

    private ActiveGameDto toActiveGameDto(Game game) {
        return new ActiveGameDto(game.getStatus().label(), toGuessDtos(game));
    }

    private List<GuessDto> toGuessDtos(Game game) {
        String word = game.getWord().getText();
        return guessRepository.findByGameOrderByIdAsc(game).stream()
                .map(g -> new GuessDto(g.getText(), ScoreUtil.score(g.getText(), word)))
                .collect(Collectors.toList());
    }
}
