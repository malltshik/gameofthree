package ru.malltshik.gameofthree.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import ru.malltshik.gameofthree.entities.Game;
import ru.malltshik.gameofthree.services.GameService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChallengeController {

    private final GameService gameService;

    @MessageMapping("/challenge/open")
    public void openChallenge(@Payload String opponent, @Header("simpSessionId") String sessionId) {
        gameService.openChallenge(sessionId, opponent);
    }

    @MessageMapping("/challenge/accept")
    public void acceptChallenge(@Payload Game game, @Header("simpSessionId") String sessionId) {
        gameService.acceptChallenge(sessionId, game);
    }

    @MessageMapping("/challenge/close")
    public void closeChallenge(@Payload Game game, @Header("simpSessionId") String sessionId) {
        gameService.closeChallenge(sessionId, game);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
}