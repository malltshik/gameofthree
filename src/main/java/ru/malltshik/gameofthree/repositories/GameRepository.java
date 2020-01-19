package ru.malltshik.gameofthree.repositories;

import ru.malltshik.gameofthree.entities.Game;

public interface GameRepository {
    Game getOne(Integer id);

    Game save(Game game);

    Game remove(Game game);

    Game removeByPlayer(String player);
}
