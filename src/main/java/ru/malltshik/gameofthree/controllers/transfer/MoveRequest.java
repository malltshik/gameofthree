package ru.malltshik.gameofthree.controllers.transfer;

import lombok.Data;

@Data
public class MoveRequest {
    private int move;
    private String opponent;
}
