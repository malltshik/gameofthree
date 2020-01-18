package ru.malltshik.gameofthree.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;
import java.util.Objects;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {

    @EqualsAndHashCode.Include
    private String player1;
    @EqualsAndHashCode.Include
    private String player2;
    private String winner;
    private Integer number;
    private boolean accepted;
    private final LinkedList<Move> moves = new LinkedList<>();

    @JsonProperty("id")
    public int getId() {
        return Objects.hash(player1, player2);
    }

    @JsonProperty("currentPlayer")
    public String getCurrentPlayer() {
        if (winner != null) return null;
        if (moves.isEmpty()) return player1;
        return moves.size() % 2 == 0 ? player1 : player2;
    }
}
