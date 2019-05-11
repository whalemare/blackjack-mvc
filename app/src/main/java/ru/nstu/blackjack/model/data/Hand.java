package ru.nstu.blackjack.model.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import ru.nstu.blackjack.model.data.Card;

/**
 * Representation of a hand of playing cards
 * <p/>
 * Created by bryancapps on 6/19/15.
 */
public class Hand implements Serializable, Iterable<Card> {
    private final List<Card> cards;
    private transient final Subject<String> events;

    public Hand() {
        cards = new ArrayList<>();
        events = PublishSubject.create();
    }

    public Observable<String> getEvents() {
        return events.hide();
    }

    public List<Card> cards() {
        return new ArrayList<>(cards);
    }

    public void addCard(Card card) {
        cards.add(card);
        events.onNext("card added " + card.toString());
    }

    public int size() {
        return cards.size();
    }

    @Deprecated
    public void draw(Card card) {
        addCard(card);
    }

    public int score() {
        int score = 0;
        boolean hasAce = false;

        for (int i = 0; i < cards().size(); i++) {
            Card card = cards().get(i);
            if (card.value() == 1) {
                hasAce = true;
            }
            score += card.value();
        }

        if (hasAce && score <= 11) {
            score += 10;
        }

        return score;
    }

    public void clear() {
        cards.clear();
        events.onNext("cards cleared");
    }

    @Override
    public Iterator<Card> iterator() {
        return cards.iterator();
    }
}
