package ru.malltshik.gameofthree.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Move {
    String author;
    Integer move;
    Integer prev;
    Integer current;
}
