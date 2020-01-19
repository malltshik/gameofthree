package ru.malltshik.gameofthree.repositories.inmemory;

import org.springframework.stereotype.Repository;
import ru.malltshik.gameofthree.entities.Game;
import ru.malltshik.gameofthree.repositories.GameRepository;

import java.util.HashMap;
import java.util.Optional;

@Repository
public class InMemoryGameRepository implements GameRepository {

    private final static HashMap<Integer, Game> STORAGE = new HashMap<>();

    @Override
    public Game getOne(Integer id) {
        return STORAGE.get(id);
    }

    @Override
    public Game save(Game game) {
        return STORAGE.put(game.getId(), game);
    }

    @Override
    public Game remove(Game game) {
        return STORAGE.remove(game.getId());
    }

    @Override
    public Game removeByPlayer(String player) {
        Optional<Game> game = STORAGE.values().stream()
                .filter(g -> g.getPlayer1().equals(player) || g.getPlayer2().equals(player))
                .findFirst();
        game.ifPresent(g -> STORAGE.remove(g.getId()));
        return game.orElse(null);
    }
}
