package ru.nstu.blackjack.controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.nstu.blackjack.model.Deck;
import ru.nstu.blackjack.model.GameData;
import ru.nstu.blackjack.model.GameState;
import ru.nstu.blackjack.model.GameStatus;
import ru.nstu.blackjack.model.PlayerState;
import ru.nstu.blackjack.model.interactor.GameInteractor;
import ru.nstu.blackjack.view.GameActivity;

/**
 * @author Anton Vlasov - whalemare
 * @since 2019
 */
public class GameController {

    public static int START_MONEY = 1000;

    private final GameActivity view;
    private final SharedPreferences settings;
    private final GameInteractor interactor = new GameInteractor();

    public GameData game;

    private ArrayList<Object> disposables;
    private CompositeDisposable disposable;

    private long pendingBet = 0;

    public GameController(GameActivity view) {
        this.view = view;
        this.settings = view.getPreferences(Context.MODE_PRIVATE);
        startNewGame();
    }

    private void startNewGame() {
        this.game = new GameData(
                settings.getLong("getMyMoney", START_MONEY),
                new Deck(interactor.getCardStack(152))
        );

        Disposable noMoney = game.getMe()
                .getObservable()
                .map(PlayerState::getStatus)
                .filter(status -> status == GameStatus.BETTING && game.getMe().getMoney() <= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> view.showResetGameDialog(START_MONEY));

        Disposable dealerHands = game.getObservable()
                .map(GameState::getDealerCards)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showDealerCards);

        Disposable monies = game.getObservable()
                .map(GameState::getMoney)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showMoney);

        Disposable playerHands = game.getMe().getObservable()
                .map(PlayerState::getCards)
                .distinctUntilChanged()
                .filter(cards -> cards.size() != 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showPlayerCards);

        Disposable bets = game.getMe().getObservable()
                .map(PlayerState::getBet)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showBet);

        Disposable statuses = game.getMe().getObservable()
                .map(PlayerState::getStatus)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    view.showGameStatus(status, game);
                });

        disposables = new ArrayList<>();
        disposable = new CompositeDisposable(dealerHands, monies, playerHands, bets, statuses);
        Collections.addAll(disposables, noMoney);

        view.showMoney(game.getMe().getMoney());
        view.showBet(pendingBet);
    }

    public void onClickDecrementBet() {
        pendingBet = interactor.decrementBet(pendingBet, game.getMe().getMoney());
        view.showBet(pendingBet);
        validateChangeBetButtons();
    }

    public void onClickIncrementBet() {
        pendingBet = interactor.incrementBet(pendingBet, game.getMe().getMoney());
        view.showBet(pendingBet);
        validateChangeBetButtons();
    }

    public void onClickResetGame() {
        game.getMe().addMoney(1000);
        game.resetForNewHand();
        startNewGame();
    }

    public void onClickBet() {
        game.getMe().initialBet(pendingBet);
        game.getMe().takeMoney(pendingBet);

        game.getDealer().getHand().addCard(game.getDeck().nextCard());
        game.getMe().getHand().addCard(game.getDeck().nextCard());
        game.getDealer().getHand().addCard(game.getDeck().nextCard());
        game.getMe().getHand().addCard(game.getDeck().nextCard());

        game.getMe().checkBlackjack();
        game.checkDealerBlackjack();
    }

    public void onClickOneMoreGame() {
        game.resetForNewHand();
    }

    private void validateChangeBetButtons() {
        boolean canIncrement = interactor.canIncrement(pendingBet, game.getMe().getMoney());
        view.enableIncrementButton(canIncrement);

        boolean canDecrement = interactor.canDecrement(pendingBet, game.getMe().getMoney());
        view.enableDecrementButton(canDecrement);

        boolean canMakeBet = interactor.canMakeBet(pendingBet, game.getMe().getMoney());
        view.enableMakeBetButton(canMakeBet);
    }

    public void onClickHit() {
        game.getMe().getHand().addCard(game.getDeck().nextCard());
        if (interactor.isGameEnd(game)) {
            game.getMe().endHand();
        }
    }

    public void onClickStay() {
        game.getMe().stay();
    }

    public void onDestroy() {
        settings.edit()
                .putLong("getMyMoney", game.getMe().getMoney())
                .apply();
    }
}
