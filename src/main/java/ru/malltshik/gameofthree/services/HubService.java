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
import ru.malltshik.gameofthree.repositories.HubRepository;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubService {

    private final static String HUB_DESTINATION = "/topic/hub";

    private final SimpMessageSendingOperations messaging;
    private final HubRepository hubRepository;

    @EventListener
    public void join(SessionSubscribeEvent event) {
        log.debug("SessionSubscribeEvent:  {}", event);
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        if (Objects.equals(headers.getDestination(), HUB_DESTINATION)) {
            hubRepository.add(headers.getSessionId());
            messaging.convertAndSend(HUB_DESTINATION, hubRepository.getAll());
        }
    }

    @EventListener
    public void leave(SessionUnsubscribeEvent event) {
        log.debug("SessionUnsubscribeEvent:  {}", event);
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        if (Objects.equals(headers.getDestination(), HUB_DESTINATION)) {
            hubRepository.remove(headers.getSessionId());
            messaging.convertAndSend(HUB_DESTINATION, hubRepository.getAll());
        }
    }

    @EventListener
    public void leave(SessionDisconnectEvent event) {
        log.debug("SessionDisconnectEvent:  {}", event);
        StompHeaderAccessor headers = StompHeaderAccessor.wrap(event.getMessage());
        hubRepository.remove(headers.getSessionId());
        messaging.convertAndSend(HUB_DESTINATION, hubRepository.getAll());
    }

    public void leave(String... sessionId) {
        hubRepository.removeAll(Arrays.asList(sessionId));
        messaging.convertAndSend(HUB_DESTINATION, hubRepository.getAll());
    }

    public void join(String... sessionId) {
        hubRepository.addAll(Arrays.asList(sessionId));
        messaging.convertAndSend(HUB_DESTINATION, hubRepository.getAll());
    }

}
