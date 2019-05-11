package ru.nstu.blackjack.model;

import java.io.Serializable;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import java8.util.stream.StreamSupport;
import ru.nstu.blackjack.utils.Utils;

public class Game implements Serializable {
    private final Deck deck;
    private long myMoney;
    private final Player me;
    private final Player dealer;
    private final transient Subject<GameState> states;

    public Game(long startMoney, Deck deck) {
        this.deck = deck;
        myMoney = startMoney;
        dealer = new Player(this, new DealerHand());
        me = new Player(this, new Hand());
        states = BehaviorSubject.create();

        dealer.getHand().getEvents().subscribe(s -> publishState());
        publishState();
    }

    public Player getMe() {
        return me;
    }

    public Observable<GameState> getObservable() {
        return states.hide().observeOn(Schedulers.computation());
    }

    private void publishState() {
        states.onNext(new GameState.GameStateBuilder()
                .setPlayerCount(players().size())
                .setMoney((int) myMoney)
                .setDealerCards(dealer.cards())
                .createGameState());
    }

    public List<Player> players() {
        return Utils.listOf(me);
    }

    public void setMyMoney(long myMoney) {
        this.myMoney = myMoney;
        publishState();
    }

    public Deck getDeck() {
        return deck;
    }

    //region View Methods

    public long getMyMoney() {
        return myMoney;
    }

    public List<Card> dealerCards() {
        return dealer.getHand().cards();
    }

    public int dealerScore() {
        return dealer.getHand().score();
    }

    //endregion

    //region Game Control

    public void resetForNewHand() {
        StreamSupport.stream(players())
                .forEach(Player::reset);

        dealer.getHand().clear();
        ((DealerHand) dealer.getHand()).setFirstCardVisibility(false);
        deck.shuffle();
    }

    public void nextCardDealer() {
        dealer.getHand().draw(deck);
    }

    public void checkDealerBlackjack() {
        if (((DealerHand) dealer.getHand()).realScore() == 21 && dealer.getHand().size() == 2) {
            for (Player player : players()) {
                player.endHand();
            }
        }
    }

    boolean shouldShowdown() {
        boolean allWaiting = true;
        for (Player player : players()) {
            if (player.status() != GameStatus.WAITING && player.status() != GameStatus.SHOWDOWN) {
                allWaiting = false;
                break;
            }
        }
        return allWaiting;
    }

    void showdown() {
        ((DealerHand) dealer.getHand()).setFirstCardVisibility(true);
        ((DealerHand) dealer.getHand()).drawUpToSeventeen(deck);
        for (Player player : players()) {
            player.setStatus(GameStatus.SHOWDOWN);
            setMyMoney(getMyMoney() + player.winnings());
        }
    }

    public Player getDealer() {
        return dealer;
    }

    //endregion

}
