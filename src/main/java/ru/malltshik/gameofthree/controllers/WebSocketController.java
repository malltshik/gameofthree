package ru.malltshik.gameofthree.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import ru.malltshik.gameofthree.controllers.transfer.MoveRequest;
import ru.malltshik.gameofthree.services.GameService;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final GameService gameService;

    @MessageMapping("/start")
    public void startGame(@Payload String opponent, @Header("simpSessionId") String sessionId) {
        gameService.startGame(sessionId, opponent);
    }

    @MessageMapping("/leave")
    public void startGame(@Header("simpSessionId") String sessionId) {
        gameService.leaveGame(sessionId);
    }

    @MessageMapping("/game/{key}")
    public void move(@DestinationVariable Integer key, @Payload Integer request, @Header("simpSessionId") String sessionId) {
        gameService.move(key, sessionId, request);
    }

    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public String handleException(Throwable exception) {
        return exception.getMessage();
    }
}