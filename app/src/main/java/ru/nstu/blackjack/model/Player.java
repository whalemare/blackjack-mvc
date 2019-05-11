package ru.nstu.blackjack.model;

import java.io.Serializable;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class Player implements Serializable {
    @Deprecated
    private final Game game;

    private final Hand hand;

    private final transient Subject<PlayerState> states;
    private long money;
    private long bet;
    private GameStatus status;

    Player(Game game, Hand hand, long startMoney) {
        this.game = game;
        this.hand = hand;
        this.money = startMoney;

        bet = 0;
        status = GameStatus.BETTING;
        states = BehaviorSubject.create();

        this.hand.getEvents().subscribe(s -> publishState());
        publishState();
    }

    public Hand getHand() {
        return hand;
    }

    public Observable<PlayerState> getObservable() {
        return states.hide().observeOn(Schedulers.computation());
    }

    private void publishState() {
        states.onNext(new PlayerState.PlayerStateBuilder()
                .setBet(getBet())
                .setCards(cards())
                .setStatus(status())
                .createPlayerState());
    }

    void reset() {
        hand.clear();
        setStatus(GameStatus.BETTING);
    }

    public long getBet() {
        return bet;
    }

    private void setBet(long bet) {
        this.bet = bet;
        publishState();
    }

    void setStatus(GameStatus status) {
        this.status = status;
        publishState();
    }

    public GameStatus status() {
        return status;
    }

    public List<Card> cards() {
        return hand.cards();
    }

    public int score() {
        return hand.score();
    }

    // called only on initial bet, not called after split for new hands
    public void initialBet(long bet) {
        setStatus(GameStatus.HITTING);
        setBet(bet);
    }

    public void checkBlackjack() {
        if(hand.score() == 21 && hand.size() == 2) {
            endHand();
        }
    }

    public void stay() {
        endHand();
    }

    public void endHand() {
        setStatus(GameStatus.WAITING);
        if (game.shouldShowdown()) {
            game.showdown();
        }
    }

    //endregion

    //region Hand Outcome

    public long winnings() {
        switch (outcome()) {
            case PLAYER_BLACKJACK:
                return Math.round(getBet() * 2.5);
            case PLAYER_WIN:
                return getBet() * 2;
            case DEALER_BUST:
                return getBet() * 2;
            case PUSH:
                return getBet();
            case DEALER_BLACKJACK:
            case DEALER_WIN:
            case PLAYER_BUST:
            default:
                return 0;
        }
    }

    public GameOutcome outcome() {
        int playerScore = hand.score();
        int dealerScore = game.dealerScore();
        int nPlayerCards = hand.size();
        int nDealerCards = game.getDealer().cards().size();

        if (dealerScore == playerScore && dealerScore <= 21) {
            // push
            return GameOutcome.PUSH;
        } else if (playerScore == 21 && nPlayerCards == 2) {
            // player has a blackjack!
            return GameOutcome.PLAYER_BLACKJACK;
        } else if (dealerScore == 21 && nDealerCards == 2) {
            // dealer has a blackjack
            return GameOutcome.DEALER_BLACKJACK;
        } else if (playerScore > dealerScore && playerScore <= 21) {
            // player wins!
            return GameOutcome.PLAYER_WIN;
        } else if (playerScore <= 21 && dealerScore > 21) {
            // dealer busts!
            return GameOutcome.DEALER_BUST;
        } else if (dealerScore > playerScore && dealerScore <= 21) {
            // dealer wins
            return GameOutcome.DEALER_WIN;
        } else if (playerScore > 21) {
            // player busts
            return GameOutcome.PLAYER_BUST;
        }
        return GameOutcome.ERROR;
    }

    public long getMoney() {
        return money;
    }

    public void addMoney(long value) {
        money += value;
        publishState();
    }

    public void takeMoney(long value) {
        money -= value;
        publishState();
    }

    //endregion

}
