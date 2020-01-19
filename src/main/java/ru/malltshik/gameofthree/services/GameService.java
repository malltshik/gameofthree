package ru.malltshik.gameofthree.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import ru.malltshik.gameofthree.entities.Game;
import ru.malltshik.gameofthree.entities.Move;
import ru.malltshik.gameofthree.repositories.GameRepository;

import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameService {

    private final SimpMessageSendingOperations messaging;
    private final HubService hubService;
    private final GameRepository gameRepository;

    public void openChallenge(String initiator, String opponent) {
        Game game = new Game(initiator, opponent, null, getRandom(), false);
        messaging.convertAndSendToUser(initiator, "/queue/game", game, getHeaders(initiator));
        messaging.convertAndSendToUser(opponent, "/queue/game", game, getHeaders(opponent));
        gameRepository.save(game);
        hubService.leave(initiator);
    }

    public void closeChallenge(String initiator, Game game) {
        messaging.convertAndSendToUser(game.getPlayer2(), "/queue/challengeClosed", game, getHeaders(game.getPlayer2()));
        gameRepository.remove(game);
        hubService.join(initiator);
    }

    public void acceptChallenge(String sessionId, Game g) {
        Game game = gameRepository.getOne(g.getId());
        game.setAccepted(true);
        messaging.convertAndSendToUser(sessionId, "/queue/game", game, getHeaders(sessionId));
        messaging.convertAndSendToUser(game.getPlayer1(), "/queue/game", game, getHeaders(game.getPlayer1()));
        hubService.leave(game.getPlayer2());
    }

    public void move(Integer gameId, String player, Integer move) {
        Game game = gameRepository.getOne(gameId);
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
            gameRepository.remove(game);
            hubService.join(game.getPlayer1(), game.getPlayer2());
        }
        game.getMoves().add(new Move(player, move, prev, result));
        String destination = String.format("/queue/game/%s/moves", game.getId());
        messaging.convertAndSendToUser(game.getPlayer1(), destination, game, getHeaders(game.getPlayer1()));
        messaging.convertAndSendToUser(game.getPlayer2(), destination, game, getHeaders(game.getPlayer2()));
    }

    @EventListener
    public void leaveGame(SessionDisconnectEvent event) {
        notifyAlonePlayer(event, false);
    }

    @EventListener
    public void leaveGame(SessionUnsubscribeEvent event) {
        notifyAlonePlayer(event, true);
    }

    private void notifyAlonePlayer(AbstractSubProtocolEvent event, boolean manual) {
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headers.getSessionId();
        Optional.ofNullable(gameRepository.removeByPlayer(sessionId)).ifPresent(g -> {
            if (g.getPlayer1().equals(sessionId)) {
                messaging.convertAndSendToUser(g.getPlayer2(), "/queue/opponentLeft", g, getHeaders(g.getPlayer2()));
                hubService.join(g.getPlayer2());
            } else {
                messaging.convertAndSendToUser(g.getPlayer1(), "/queue/opponentLeft", g, getHeaders(g.getPlayer1()));
                hubService.join(g.getPlayer1());
            }
            if (manual) {
                hubService.join(sessionId);
            }
        });
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
