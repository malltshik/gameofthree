package ru.malltshik.gameofthree.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import ru.malltshik.gameofthree.entities.Game;
import ru.malltshik.gameofthree.entities.Move;

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

    public void openChallenge(String initiator, String opponent) {
        Game game = new Game(initiator, opponent, null, getRandom(), false);
        messaging.convertAndSendToUser(initiator, "/queue/game", game, getHeaders(initiator));
        messaging.convertAndSendToUser(opponent, "/queue/game", game, getHeaders(opponent));
        GAMES.put(game.getId(), game);
        hubService.leave(initiator);
    }

    public void closeChallenge(String initiator, Game game) {
        messaging.convertAndSendToUser(game.getPlayer2(), "/queue/challengeClosed", game, getHeaders(game.getPlayer2()));
        GAMES.remove(game.getId(), game);
        hubService.join(initiator);
    }

    @EventListener
    public void leaveGame(SessionDisconnectEvent event) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        GAMES.entrySet().removeIf(e -> {
            Game game = e.getValue();
            String alone = null;
            if (game.getPlayer1().equals(sessionId)) {
                alone = game.getPlayer2();
            }
            if (game.getPlayer2().equals(sessionId)) {
                alone = game.getPlayer1();
            }
            if (alone != null) {
                messaging.convertAndSendToUser(alone, "/queue/opponentLeft", game, getHeaders(alone));
                return true;
            } else {
                return false;
            }
        });
    }

    public void acceptChallenge(String sessionId, Game g) {
        Game game = GAMES.get(g.getId());
        game.setAccepted(true);
        messaging.convertAndSendToUser(sessionId, "/queue/game", game, getHeaders(sessionId));
        messaging.convertAndSendToUser(game.getPlayer1(), "/queue/game", game, getHeaders(game.getPlayer1()));
        hubService.leave(game.getPlayer2());
    }

    public void move(Integer gameId, String player, Integer move) {
        Game game = GAMES.get(gameId);
        if (game == null) {
            throw new RuntimeException("Game not found!");
        }
        if (!game.getCurrentPlayer().equals(player)) {
            throw new RuntimeException("Not your turn!");
        }
        int prev;
        if (game.getMoves().isEmpty()) {
            prev = game.getNumber();
        } else {
            prev = game.getMoves().getLast().getCurrent();
        }
        if ((prev + move) % 3 != 0) {
            throw new RuntimeException("Result not divided by 3!");
        }
        int result = (prev + move) / 3;
        if (result == 1) {
            game.setWinner(player);
            GAMES.remove(gameId);
            hubService.join(game.getPlayer1(), game.getPlayer2());
        }
        game.getMoves().add(new Move(player, move, prev, result));
        String destination = String.format("/queue/game/%s/moves", game.getId());
        messaging.convertAndSendToUser(game.getPlayer1(), destination, game, getHeaders(game.getPlayer1()));
        messaging.convertAndSendToUser(game.getPlayer2(), destination, game, getHeaders(game.getPlayer2()));
    }

    private Map<String, Object> getHeaders(String sessionId) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        return headerAccessor.getMessageHeaders();
    }

    private Integer getRandom() {
        return new Random().nextInt((1000 - 100) + 1) + 100;
    }
}
