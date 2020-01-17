package ru.malltshik.gameofthree.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.stereotype.Component;
import ru.malltshik.gameofthree.entities.Game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameService {

    private final static HashMap<Integer, Game> GAMES = new HashMap<>();
    private final SimpMessageSendingOperations messaging;
    private final HubService hubService;

    public void startGame(String initiator, String opponent) {
        Game game = new Game(initiator, opponent, new Random().nextInt((99 - 10) + 1) + 10, 1);
        int gameId = game.hashCode();
        hubService.leave(initiator);
        messaging.convertAndSendToUser(initiator, "/queue/game", new Invite(gameId, opponent), getHeaders(initiator));
        messaging.convertAndSendToUser(opponent, "/queue/game", new Invite(gameId, initiator), getHeaders(opponent));
        GAMES.put(gameId, game);
    }

    public void move(Integer gameId, String player, Integer request) {
        Game game = GAMES.get(gameId);
        if (game == null) {
            throw new RuntimeException("Game doesn't exist");
        }
        if (player.equals(game.getPlayer1()) && game.getMoveTurn() != 0) {
            throw new RuntimeException("Another player turn");
        }
        int i = game.getNumber() + request / 3;
        game.setNumber(i);
        game.getSteps().add(request);
    }

    public void leaveGame(String sessionId) {

    }

    @Data
    @AllArgsConstructor
    private static class Invite {
        int gameId;
        String opponent;
    }

    private Map<String, Object> getHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }
}
