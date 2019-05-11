package ru.nstu.blackjack.model.data;

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

    /**
     * Ожидание
     */
    WAITING,

    /**
     * Демонстрация карт и подсчет очков
     */
    SHOWDOWN
}