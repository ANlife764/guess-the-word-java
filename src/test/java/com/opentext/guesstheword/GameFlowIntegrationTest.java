package com.opentext.guesstheword;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opentext.guesstheword.model.AppUser;
import com.opentext.guesstheword.model.Game;
import com.opentext.guesstheword.model.GameStatus;
import com.opentext.guesstheword.model.Word;
import com.opentext.guesstheword.repository.AppUserRepository;
import com.opentext.guesstheword.repository.GameRepository;
import com.opentext.guesstheword.repository.WordRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Each test runs inside a rolled-back transaction (@Transactional), so tests don't
 * interfere with each other even though they share one in-memory database per class run.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GameFlowIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired WordRepository wordRepository;
    @Autowired GameRepository gameRepository;
    @Autowired AppUserRepository userRepository;
    @Autowired ObjectMapper objectMapper;

    private MockHttpSession registerAndLogin(String username, String password) throws Exception {
        mockMvc.perform(post("/register").param("username", username).param("password", password))
                .andExpect(status().is3xxRedirection());
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", username).param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }

    private void forceActiveGameWord(AppUser user, String text) {
        Game game = gameRepository.findFirstByUserAndStatus(user, GameStatus.ACTIVE).orElseThrow();
        Word word = wordRepository.findAll().stream()
                .filter(w -> w.getText().equals(text)).findFirst()
                .orElseGet(() -> wordRepository.save(new Word(text)));
        game.setWord(word);
        gameRepository.save(game);
    }

    @Test
    void twentyWordsAreSeeded() {
        assertThat(wordRepository.count()).isEqualTo(20);
    }

    @Test
    void adminAccountIsSeeded() {
        assertThat(userRepository.findByUsernameLower("admin")).isPresent();
    }

    @Test
    void winFlow() throws Exception {
        MockHttpSession session = registerAndLogin("Player", "pass$1");
        mockMvc.perform(post("/api/game/start").session(session)).andExpect(status().isOk());

        AppUser user = userRepository.findByUsernameLower("player").orElseThrow();
        forceActiveGameWord(user, "TOWER");

        mockMvc.perform(post("/api/game/guess").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guess\":\"HOMER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("active"));

        mockMvc.perform(post("/api/game/guess").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guess\":\"TOWER\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("won"))
                .andExpect(jsonPath("$.guesses.length()").value(2))
                .andExpect(jsonPath("$.word").value("TOWER"));
    }

    @Test
    void invalidGuessesAreRejectedWithoutBeingCounted() throws Exception {
        MockHttpSession session = registerAndLogin("Player2", "pass$1");
        mockMvc.perform(post("/api/game/start").session(session)).andExpect(status().isOk());
        AppUser user = userRepository.findByUsernameLower("player2").orElseThrow();
        forceActiveGameWord(user, "TOWER");

        mockMvc.perform(post("/api/game/guess").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"guess\":\"tower\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/game/guess").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"guess\":\"ABC\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/game/guess").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"guess\":\"ZZZZZ\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(Matchers.containsString("not in the word list")));

        // None of the rejected attempts should have used up a real guess.
        mockMvc.perform(get("/api/game").session(session))
                .andExpect(jsonPath("$.game.guesses.length()").value(0));
    }

    @Test
    void loseAfterFiveGuessesAndDailyCap() throws Exception {
        MockHttpSession session = registerAndLogin("Player3", "pass$1");
        AppUser user = userRepository.findByUsernameLower("player3").orElseThrow();

        for (int word = 0; word < 3; word++) {
            mockMvc.perform(post("/api/game/start").session(session)).andExpect(status().isOk());
            forceActiveGameWord(user, "TOWER");
            String lastStatus = null;
            for (int i = 0; i < 5; i++) {
                MvcResult r = mockMvc.perform(post("/api/game/guess").session(session)
                                .contentType(MediaType.APPLICATION_JSON).content("{\"guess\":\"AUDIO\"}"))
                        .andExpect(status().isOk()).andReturn();
                lastStatus = objectMapper.readTree(r.getResponse().getContentAsString())
                        .get("status").asText();
            }
            assertThat(lastStatus).isEqualTo("lost");
        }
        // A 4th word on the same day must be refused.
        mockMvc.perform(post("/api/game/start").session(session)).andExpect(status().isBadRequest());
    }

    @Test
    void adminReportsAndAccessControl() throws Exception {
        MockHttpSession playerSession = registerAndLogin("Player4", "pass$1");
        mockMvc.perform(post("/api/game/start").session(playerSession)).andExpect(status().isOk());
        AppUser user = userRepository.findByUsernameLower("player4").orElseThrow();
        forceActiveGameWord(user, "TOWER");
        mockMvc.perform(post("/api/game/guess").session(playerSession)
                .contentType(MediaType.APPLICATION_JSON).content("{\"guess\":\"TOWER\"}"));

        // Players cannot reach /admin.
        mockMvc.perform(get("/admin").session(playerSession)).andExpect(status().is3xxRedirection());

        MockHttpSession adminSession = registerAdminSessionOnly();

        mockMvc.perform(get("/admin").session(adminSession).param("username", "Player4"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Player4")));

        // Admin cannot use the player API.
        mockMvc.perform(get("/api/game").session(adminSession)).andExpect(status().isForbidden());
    }

    private MockHttpSession registerAdminSessionOnly() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/login")
                        .param("username", "admin").param("password", "Admin$123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        return (MockHttpSession) loginResult.getRequest().getSession();
    }

    @Test
    void duplicateUsernameIsRejectedCaseInsensitively() throws Exception {
        registerAndLogin("Racer", "pass$1");
        mockMvc.perform(post("/register").param("username", "RACER").param("password", "pass$1"))
                .andExpect(status().is3xxRedirection());
        long count = userRepository.findAll().stream()
                .filter(u -> "racer".equals(u.getUsernameLower())).count();
        assertThat(count).isEqualTo(1);
    }
}
