package com.opentext.guesstheword.controller;

import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Game;
import com.opentext.guesstheword.model.GameStatus;
import com.opentext.guesstheword.model.Role;
import com.opentext.guesstheword.repository.AppUserRepository;
import com.opentext.guesstheword.repository.GameRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class AdminController {

    public record UserReportRow(LocalDate date, long tried, long correct) {
    }

    private final GameRepository gameRepository;
    private final AppUserRepository userRepository;

    public AdminController(GameRepository gameRepository, AppUserRepository userRepository) {
        this.gameRepository = gameRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/admin")
    public String admin(@RequestParam(required = false) String date,
                         @RequestParam(required = false) String username,
                         Model model) {
        LocalDate day = (date == null || date.isBlank()) ? LocalDate.now() : LocalDate.parse(date);

        long dailyUsers = gameRepository.countDistinctUsersOnDate(day);
        long dailyCorrect = gameRepository.countByPlayDateAndStatus(day, GameStatus.WON);

        List<String> players = userRepository.findByRoleOrderByUsernameAsc(Role.PLAYER).stream()
                .map(AppUser::getUsername).toList();
        String selected = (username == null || username.isBlank())
                ? (players.isEmpty() ? "" : players.get(0)) : username;

        List<UserReportRow> perUser = List.of();
        if (!selected.isEmpty()) {
            AppUser user = userRepository.findByUsernameLower(selected.toLowerCase()).orElse(null);
            if (user != null) {
                Map<LocalDate, long[]> byDate = new LinkedHashMap<>(); // [tried, correct]
                for (Game g : gameRepository.findByUserOrderByPlayDateAsc(user)) {
                    long[] counts = byDate.computeIfAbsent(g.getPlayDate(), d -> new long[2]);
                    counts[0]++;
                    if (g.getStatus() == GameStatus.WON) {
                        counts[1]++;
                    }
                }
                perUser = byDate.entrySet().stream()
                        .map(e -> new UserReportRow(e.getKey(), e.getValue()[0], e.getValue()[1]))
                        .toList();
            }
        }

        model.addAttribute("day", day.toString());
        model.addAttribute("dailyUsers", dailyUsers);
        model.addAttribute("dailyCorrect", dailyCorrect);
        model.addAttribute("players", players);
        model.addAttribute("username", selected);
        model.addAttribute("perUser", perUser);
        return "admin";
    }
}
