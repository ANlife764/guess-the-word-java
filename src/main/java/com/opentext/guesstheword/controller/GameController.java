package com.opentext.guesstheword.controller;

import com.opentext.guesstheword.dto.GameDtos.ErrorDto;
import com.opentext.guesstheword.dto.GameDtos.GuessRequest;
import com.opentext.guesstheword.dto.GameDtos.StateDto;
import com.opentext.guesstheword.dto.GameDtos.GuessResultDto;
import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.repository.AppUserRepository;
import com.opentext.guesstheword.service.GameException;
import com.opentext.guesstheword.service.GameService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class GameController {

    private final GameService gameService;
    private final AppUserRepository userRepository;

    public GameController(GameService gameService, AppUserRepository userRepository) {
        this.gameService = gameService;
        this.userRepository = userRepository;
    }

    @GetMapping("/play")
    public String playPage() {
        return "play";
    }

    @GetMapping("/api/game")
    @ResponseBody
    public StateDto apiState(HttpSession session) {
        return gameService.getState(currentUser(session));
    }

    @PostMapping("/api/game/start")
    @ResponseBody
    public StateDto apiStart(HttpSession session) {
        return gameService.startGame(currentUser(session));
    }

    @PostMapping("/api/game/guess")
    @ResponseBody
    public GuessResultDto apiGuess(@RequestBody GuessRequest body, HttpSession session) {
        String guess = body == null || body.guess() == null ? "" : body.guess().trim().toUpperCase();
        return gameService.submitGuess(currentUser(session), guess);
    }

    @ExceptionHandler(GameException.class)
    @ResponseBody
    public ResponseEntity<ErrorDto> handleGameException(GameException e) {
        return ResponseEntity.status(e.getStatus()).body(new ErrorDto(e.getMessage()));
    }

    private AppUser currentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        // AuthInterceptor already guarantees a logged-in PLAYER reaches this point.
        return userRepository.findById(userId)
                .orElseThrow(() -> new GameException("Session user no longer exists."));
    }
}
