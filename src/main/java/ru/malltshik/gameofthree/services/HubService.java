package ru.malltshik.gameofthree.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubService {

    private final static ObjectMapper MAPPER = new ObjectMapper();
    private final static Set<String> PLAYERS = new HashSet<>();
    private final static ScheduledThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(1);

    private final SimpMessageSendingOperations messagingTemplate;

    @EventListener
    public void handleSessionConnected(SessionConnectEvent event) throws JsonProcessingException {
        log.debug("Session connected {}", event);
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        if (sessionId != null) {
            PLAYERS.add(sessionId);
            String players = MAPPER.writeValueAsString(PLAYERS);
            EXECUTOR.schedule(() -> messagingTemplate.convertAndSend("/topic/hub", players), 1, TimeUnit.SECONDS);
        }
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) throws JsonProcessingException {
        log.debug("Session disconnected {}", event);
        String sessionId = (String) event.getMessage().getHeaders().get("simpSessionId");
        if (sessionId != null) {
            PLAYERS.remove(sessionId);
            String players = MAPPER.writeValueAsString(PLAYERS);
            EXECUTOR.schedule(() -> messagingTemplate.convertAndSend("/topic/hub", players), 1, TimeUnit.SECONDS);
        }
    }
}
