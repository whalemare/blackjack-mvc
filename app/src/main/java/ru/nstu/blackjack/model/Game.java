package ru.nstu.blackjack.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;

public class Game implements Serializable {
    private final Deck deck;
    private long money;
    private final DealerHand dealerHand;
    private final List<Player> players;
    private final transient Subject<GameState> states;

    public Game() {
        deck = new Deck(new Random());
        money = 1000;
        dealerHand = new DealerHand();
        players = new ArrayList<>();
        states = BehaviorSubject.create();

        dealerHand.getEvents().subscribe(s -> publishState());
        publishState();
    }

    public Observable<GameState> getObservable() {
        return states.hide().observeOn(Schedulers.computation());
    }

    private void publishState() {
        states.onNext(new GameState.GameStateBuilder()
                .setPlayerCount(players().size())
                .setMoney((int) money)
                .setDealerCards(dealerCards())
                .createGameState());
    }

    public List<Player> players() {
        return new ArrayList<>(players);
    }

    public void setMoney(long money) {
        this.money = money;
        publishState();
    }

    Deck deck() {
        return deck;
    }

    //region View Methods

    public long money() {
        return money;
    }

    public List<Card> dealerCards() {
        return dealerHand.cards();
    }

    public Player newPlayer() {
        Player newPlayer = new Player(this, new Hand());
        players.add(newPlayer);
        publishState();
        return newPlayer;
    }

    public int dealerScore() {
        return dealerHand.score();
    }

    //endregion

    //region Game Control

    public void resetForNewHand() {
        while (players.size() > 1) {
            players.remove(players.size() - 1);
            publishState();
        }
        players.get(0).reset();
        dealerHand.clear();
        dealerHand.setFirstCardVisibility(false);
        deck.shuffle();
    }

    void drawCardForDealer() {
        dealerHand.draw(deck);
    }

    void checkDealerBlackjack() {
        if (dealerHand.realScore() == 21 && dealerHand.size() == 2) {
            for (Player player : players) {
                player.endHand();
            }
        }
    }

    boolean shouldShowdown() {
        boolean allWaiting = true;
        for (Player player : players) {
            if (player.status() != GameStatus.WAITING && player.status() != GameStatus.SHOWDOWN) {
                allWaiting = false;
                break;
            }
        }
        return allWaiting;
    }

    void showdown() {
        dealerHand.setFirstCardVisibility(true);
        dealerHand.drawUpToSeventeen(deck);
        for (Player player : players) {
            player.setStatus(GameStatus.SHOWDOWN);
            setMoney(money() + player.winnings());
        }
    }

    //endregion

}
