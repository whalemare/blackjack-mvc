package ru.nstu.blackjack.model;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import ru.nstu.blackjack.model.data.Card;
import ru.nstu.blackjack.model.data.Deck;

/**
 * Created by bryancapps on 12/22/16.
 */

public class DealerHand extends Hand {
    private final transient Subject<Boolean> events;
    private boolean firstCardVisibility;

    DealerHand() {
        super();
        firstCardVisibility = false;
        events = PublishSubject.create();
    }

    @Override
    public Observable<String> getEvents() {
        return Observable.merge(super.getEvents(),
                events.hide()
                        .map(b -> String.format("firstCardVisibility set to %b", b)));
    }

    void setFirstCardVisibility(boolean firstCardVisibility) {
        this.firstCardVisibility = firstCardVisibility;
        events.onNext(firstCardVisibility);
    }

    public int realScore() {
        if (firstCardVisibility) {
            return score();
        } else {
            Card firstCard = super.cards().get(0);
            int score = score() + firstCard.value();
            if (score <= 11 && firstCard.getRank() == Card.Rank.ACE) {
                score = score + 10;
            }
            return score;
        }
    }

    @Override
    public List<Card> cards() {
        List<Card> cards = super.cards();
        if (firstCardVisibility || cards.isEmpty()) {
            return cards;
        } else {
            List<Card> list = new ArrayList<>();
            list.add(Card.dealerBlank);
            list.addAll(cards.subList(1, cards.size()));
            return list;
        }
    }

    void takeWhileLower(Deck deck, int value) {
        while (score() < value) {
            addCard(deck.nextCard());
        }
    }
}
