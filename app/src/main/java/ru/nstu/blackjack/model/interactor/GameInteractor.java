package ru.nstu.blackjack.model.interactor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.nstu.blackjack.model.data.DealerHand;
import ru.nstu.blackjack.model.data.GameData;
import ru.nstu.blackjack.model.data.Hand;
import ru.nstu.blackjack.model.data.Player;
import ru.nstu.blackjack.model.data.Card;
import ru.nstu.blackjack.model.data.GameOutcomeStatus;
import ru.nstu.blackjack.model.data.GameStatus;

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

    public List<Card> getCardStack(int size) {
        List<Card> cards = new ArrayList<>(size);
        for (Card.Rank rank : Card.Rank.values()) {
            if (rank == Card.Rank.BLANK) continue;
            for (Card.Suit suit : Card.Suit.values()) {
                if (suit == Card.Suit.DEALER || suit == Card.Suit.PLAYER) continue;
                cards.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(cards);
        return cards;
    }

    public boolean isOverscore(Player player) {
        return player.getHand().score() > 21;
    }

    public boolean isBlackjack(Hand hand) {
        return ((hand instanceof DealerHand)
                ? ((DealerHand) hand).realScore()
                : hand.score()
        ) == 21 && hand.size() == 2;
    }

    public Long calculateWinnings(GameOutcomeStatus status, long bet) {
        switch (status) {
            case PLAYER_BLACKJACK:
                return Math.round(bet * 2.5);
            case PLAYER_WIN:
                return bet * 2;
            case DEALER_BUST:
                return bet * 2;
            case PUSH:
                return bet;
            case DEALER_BLACKJACK:
            case DEALER_WIN:
            case PLAYER_BUST:
                return 0L;
            default:
                throw new UnsupportedOperationException("Not handled getStatus " + status);
        }
    }

    public boolean isGameShouldShowdown(GameData game) {
        boolean allWaiting = true;
        if (game.getMe().getStatus() != GameStatus.WAITING && game.getMe().getStatus() != GameStatus.SHOWDOWN) {
            allWaiting = false;
        }
        return allWaiting;
    }

    public GameOutcomeStatus outcome(Player me, Player dealer) {
        int playerScore = me.getHand().score();
        int dealerScore = dealer.getHand().score();
        int nPlayerCards = me.getHand().size();
        int nDealerCards = dealer.cards().size();

        if (dealerScore == playerScore && dealerScore <= 21) {
            // push
            return GameOutcomeStatus.PUSH;
        } else if (playerScore == 21 && nPlayerCards == 2) {
            // player has a blackjack!
            return GameOutcomeStatus.PLAYER_BLACKJACK;
        } else if (dealerScore == 21 && nDealerCards == 2) {
            // dealer has a blackjack
            return GameOutcomeStatus.DEALER_BLACKJACK;
        } else if (playerScore > dealerScore && playerScore <= 21) {
            // player wins!
            return GameOutcomeStatus.PLAYER_WIN;
        } else if (playerScore <= 21 && dealerScore > 21) {
            // dealer busts!
            return GameOutcomeStatus.DEALER_BUST;
        } else if (dealerScore > playerScore && dealerScore <= 21) {
            // dealer wins
            return GameOutcomeStatus.DEALER_WIN;
        } else if (playerScore > 21) {
            // player busts
            return GameOutcomeStatus.PLAYER_BUST;
        }
        return GameOutcomeStatus.ERROR;
    }

    public void endHand(GameData game, Player player) {
        player.setStatus(GameStatus.WAITING);

        if (isGameShouldShowdown(game)) {
            showdown(game);
        }
    }

    public void showdown(GameData game) {
        ((DealerHand) game.getDealer().getHand()).setFirstCardVisibility(true);
        ((DealerHand) game.getDealer().getHand()).takeWhileLower(game.getDeck(), 17);
        game.getMe().setStatus(GameStatus.SHOWDOWN);
        game.getMe().addMoney(calculateWinnings(outcome(game.getMe(), game.getDealer()), game.getMe().getBet()));
    }
}
