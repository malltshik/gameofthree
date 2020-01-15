package ru.malltshik.gameofthree.entities;

import lombok.Data;

import java.util.LinkedList;

@Data
public class Game {
    private String player1;
    private String player2;
    private LinkedList<Integer> steps = new LinkedList<>();
}
