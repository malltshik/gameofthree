package ru.malltshik.gameofthree.controllers.transfer;

import lombok.Data;

@Data
public class MoveResponse {
    int currentNumber;
    String moveBy;
    String changes;
    boolean winner;
}
