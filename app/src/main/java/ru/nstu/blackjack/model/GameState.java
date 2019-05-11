package ru.nstu.blackjack.model;

import java.util.List;

/**
 * Created by bryancapps on 12/27/16.
 */
public class GameState {

    int money;
    List<Card> dealerCards;
    int playerCount;

    public GameState(int money, List<Card> dealerCards, int playerCount) {
        this.money = money;
        this.dealerCards = dealerCards;
        this.playerCount = playerCount;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public List<Card> getDealerCards() {
        return dealerCards;
    }

    public void setDealerCards(List<Card> dealerCards) {
        this.dealerCards = dealerCards;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public static class GameStateBuilder {
        private int money;
        private List<Card> dealerCards;
        private int playerCount;

        public GameStateBuilder setMoney(int money) {
            this.money = money;
            return this;
        }

        public GameStateBuilder setDealerCards(List<Card> dealerCards) {
            this.dealerCards = dealerCards;
            return this;
        }

        public GameStateBuilder setPlayerCount(int playerCount) {
            this.playerCount = playerCount;
            return this;
        }

        public GameState createGameState() {
            return new GameState(money, dealerCards, playerCount);
        }
    }
}
