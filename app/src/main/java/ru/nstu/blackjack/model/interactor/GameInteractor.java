package ru.nstu.blackjack.model.interactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.nstu.blackjack.model.Card;

/**
 * @author Anton Vlasov - whalemare
 * @since 2019
 */
public class GameInteractor {

    private static final int BET_STEP = 100;

    public long decrementBet(long currentBet, long myMoney) {
        if (myMoney < currentBet) {
            return myMoney;
        } else {
            return Math.max(0, currentBet - BET_STEP);
        }
    }

    public long incrementBet(long currentBet, long max) {
        return Math.min(max, currentBet + BET_STEP);
    }

    public boolean canIncrement(long bet, long moneyCount) {
        return moneyCount >= bet;
    }

    public boolean canDecrement(long bet, long myMoney) {
        return bet != 0 && myMoney >= bet;
    }

    public boolean canMakeBet(long pendingBet, long myMoney) {
        return pendingBet != 0 && pendingBet <= myMoney;
    }

    public boolean canDouble(long myMoney, long bet, long cardsSize) {
        return myMoney >= bet && cardsSize == 2;
    }

    public List<Card> getCardStack(int size) {
        List<Card> cards = new ArrayList<>(size);
        for (Card.Rank rank : Card.Rank.values()) {
            if (rank == Card.Rank.BLANK) continue;
            for (Card.Suit suit : Card.Suit.values()) {
                if (suit == Card.Suit.DEALER || suit == Card.Suit.PLAYER) continue;
                cards.add(Card.create(rank, suit));
            }
        }
        Collections.shuffle(cards);
        return cards;
    }
}
