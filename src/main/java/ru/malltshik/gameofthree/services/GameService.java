package ru.malltshik.gameofthree.services;

import org.springframework.stereotype.Component;
import ru.malltshik.gameofthree.entities.Game;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameService {

    private final static Map<String, Game> GAMES = new HashMap<>();



}
