package ru.nstu.blackjack.model;

public enum GameStatus {
    /**
     * В ожидании ставки
     * можно увеличивать и уменьшать ставку
     */
    BETTING,

    /**
     * Процесс игры
     * Можно брать карты еще, либо закончить игру
     */
    HITTING,

    WAITING,
    SHOWDOWN
}