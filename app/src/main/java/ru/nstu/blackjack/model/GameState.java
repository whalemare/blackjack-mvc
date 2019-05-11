package ru.nstu.blackjack.model;

import java.util.List;

/**
 * Created by bryancapps on 12/27/16.
 */
public class GameState {

    private int money;
    private List<Card> dealerCards;

    public GameState(int money, List<Card> dealerCards) {
        this.money = money;
        this.dealerCards = dealerCards;
    }

    public int getMoney() {
        return money;
    }

    public List<Card> getDealerCards() {
        return dealerCards;
    }

    public static class GameStateBuilder {
        private int money;
        private List<Card> dealerCards;

        public GameStateBuilder setMoney(int money) {
            this.money = money;
            return this;
        }

        public GameStateBuilder setDealerCards(List<Card> dealerCards) {
            this.dealerCards = dealerCards;
            return this;
        }

        public GameState createGameState() {
            return new GameState(money, dealerCards);
        }
    }
}
