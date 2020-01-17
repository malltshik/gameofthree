package ru.malltshik.gameofthree.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubService {

    private final static String HUB_DESTINATION = "/topic/hub";
    private final static Set<String> PLAYERS = new HashSet<>();

    private final SimpMessageSendingOperations messaging;

    @EventListener
    public void join(SessionSubscribeEvent event) {
        log.debug("SessionSubscribeEvent:  {}", event);
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        if (Objects.equals(headers.getDestination(), HUB_DESTINATION)) {
            PLAYERS.add(headers.getSessionId());
            messaging.convertAndSend(HUB_DESTINATION, PLAYERS);
        }
    }

    @EventListener
    public void leave(SessionUnsubscribeEvent event) {
        log.debug("SessionUnsubscribeEvent:  {}", event);
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        if (Objects.equals(headers.getDestination(), HUB_DESTINATION)) {
            PLAYERS.remove(headers.getSessionId());
            messaging.convertAndSend(HUB_DESTINATION, PLAYERS);
        }
    }

    @EventListener
    public void leave(SessionDisconnectEvent event) {
        log.debug("SessionDisconnectEvent:  {}", event);
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        PLAYERS.remove(headers.getSessionId());
        messaging.convertAndSend(HUB_DESTINATION, PLAYERS);
    }

    public void leave(String sessionId) {
        PLAYERS.remove(sessionId);
        messaging.convertAndSend(HUB_DESTINATION, PLAYERS);
    }

    public void join(String sessionId) {
        PLAYERS.add(sessionId);
        messaging.convertAndSend(HUB_DESTINATION, PLAYERS);
    }

}
