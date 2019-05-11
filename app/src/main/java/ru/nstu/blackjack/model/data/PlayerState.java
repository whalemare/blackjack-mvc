package ru.nstu.blackjack.model.data;

import java.util.List;

/**
 * Created by bryancapps on 12/27/16.
 */
public class PlayerState {

    private long bet;
    private List<Card> cards;
    private GameStatus status;

    public PlayerState(long bet, List<Card> cards, GameStatus status) {
        this.bet = bet;
        this.cards = cards;
        this.status = status;
    }

    public long getBet() {
        return bet;
    }

    public List<Card> getCards() {
        return cards;
    }

    public GameStatus getStatus() {
        return status;
    }

    public void setStatus(GameStatus status) {
        this.status = status;
    }

    public static class PlayerStateBuilder {
        private long bet;
        private List<Card> cards;
        private GameStatus status;

        public PlayerStateBuilder setBet(long bet) {
            this.bet = bet;
            return this;
        }

        public PlayerStateBuilder setCards(List<Card> cards) {
            this.cards = cards;
            return this;
        }

        public PlayerStateBuilder setStatus(GameStatus status) {
            this.status = status;
            return this;
        }

        public PlayerState createPlayerState() {
            return new PlayerState(bet, cards, status);
        }
    }
}
