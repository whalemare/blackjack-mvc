package ru.nstu.blackjack.model.data;

import java.io.Serializable;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;


public class Player implements Serializable {
    private final Hand hand;

    private final transient Subject<PlayerState> states;
    private long money;
    private long bet;
    private GameStatus status;

    Player(Hand hand, long startMoney) {
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
                .setStatus(getStatus())
                .createPlayerState());
    }

    public long getBet() {
        return bet;
    }

    private void setBet(long bet) {
        this.bet = bet;
        publishState();
    }

    public void setStatus(GameStatus status) {
        this.status = status;
        publishState();
    }

    public GameStatus getStatus() {
        return status;
    }

    public List<Card> cards() {
        return hand.cards();
    }

    /**
     * Вызывается только для начальной ставки
     */
    public void initialBet(long bet) {
        setStatus(GameStatus.HITTING);
        setBet(bet);
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

}
