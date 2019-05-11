package ru.nstu.blackjack.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Deck implements Serializable {
    private final List<Card> cards;

    public Deck(List<Card> cards) {
        this.cards = cards;
    }

    public Card nextCard() {
        return cards.remove(0);
    }

    @Deprecated
    void shuffle() {
        Collections.shuffle(cards, new Random());
    }
}
