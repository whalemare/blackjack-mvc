package ru.nstu.blackjack.controller;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Collections;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.nstu.blackjack.model.Deck;
import ru.nstu.blackjack.model.Game;
import ru.nstu.blackjack.model.GameState;
import ru.nstu.blackjack.model.GameStatus;
import ru.nstu.blackjack.model.Player;
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

    public Game game;
    public Player player;

    private ArrayList<Object> disposables;
    private CompositeDisposable disposable;

    private long pendingBet = 0;

    public GameController(GameActivity view) {
        this.view = view;
        this.settings = view.getPreferences(Context.MODE_PRIVATE);
        startNewGame();
    }

    private void startNewGame() {
        this.game = new Game(
                settings.getLong("getMyMoney", START_MONEY),
                new Deck(interactor.getCardStack(52))
        );
        this.player = game.newPlayer();

//        Disposable listsOfPlayers = game.getObservable()
//                .map(GameState::getPlayerCount)
//                .distinctUntilChanged()
//                .map(count -> game.players())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(view::showPlayers);

        Disposable noMoney = game.players()
                .get(0)
                .getObservable()
                .map(PlayerState::getStatus)
                .filter(status -> status == GameStatus.BETTING && game.getMyMoney() <= 0)
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

        Disposable playerHands = player.getObservable()
                .map(PlayerState::getCards)
                .distinctUntilChanged()
                .filter(cards -> cards.size() != 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showPlayerCards);

        Disposable bets = player.getObservable()
                .map(PlayerState::getBet)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showBet);

        Disposable statuses = player.getObservable()
                .map(PlayerState::getStatus)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::showGameStatus);

        disposables = new ArrayList<>();
        disposable = new CompositeDisposable(dealerHands, monies, playerHands, bets, statuses);
        Collections.addAll(disposables, noMoney);

        view.showMoney(game.getMyMoney());
        view.showBet(pendingBet);
    }

    public void onClickDecrementBet() {
        pendingBet = interactor.decrementBet(pendingBet, game.getMyMoney());
        view.showBet(pendingBet);
        validateChangeBetButtons();
    }

    public void onClickIncrementBet() {
        pendingBet = interactor.incrementBet(pendingBet, game.getMyMoney());
        view.showBet(pendingBet);
        validateChangeBetButtons();
    }

    public void onClickResetGame() {
        game.setMyMoney(1000);
        game.resetForNewHand();
        startNewGame();
    }

    public void onClickBet() {
        player.initialBet(pendingBet);
        game.setMyMoney(game.getMyMoney() - pendingBet);
        game.drawCardForDealer();
        player.draw();
        game.drawCardForDealer();
        player.draw();

        player.checkBlackjack();
        game.checkDealerBlackjack();
    }

    public void onClickOneMoreGame() {
        game.resetForNewHand();
    }

    private void validateChangeBetButtons() {
        boolean canIncrement = interactor.canIncrement(pendingBet, game.getMyMoney());
        view.enableIncrementButton(canIncrement);

        boolean canDecrement = interactor.canDecrement(pendingBet, game.getMyMoney());
        view.enableDecrementButton(canDecrement);

        boolean canMakeBet = interactor.canMakeBet(pendingBet, game.getMyMoney());
        view.enableMakeBetButton(canMakeBet);
    }

    public void onClickHit() {
        player.hit();
    }

    public void onClickStay() {
        player.stay();
    }

    public void onDestroy() {
        settings.edit()
                .putLong("getMyMoney", game.getMyMoney())
                .apply();
    }
}
