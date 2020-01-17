package ru.malltshik.gameofthree.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.LinkedList;

@Data
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Game {
    @EqualsAndHashCode.Include
    private String player1;
    @EqualsAndHashCode.Include
    private String player2;
    private Integer number;
    private int moveTurn;
    private final LinkedList<Integer> steps = new LinkedList<>();
}
