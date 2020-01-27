package ru.malltshik.gameofthree.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import ru.malltshik.gameofthree.services.GameService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @MessageMapping("/game/{gameId}/move")
    public void move(@Payload Integer move,
                     @DestinationVariable("gameId") Integer gameId,
                     @Header("simpSessionId") String sessionId) {
        gameService.move(gameId, sessionId, move);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
}