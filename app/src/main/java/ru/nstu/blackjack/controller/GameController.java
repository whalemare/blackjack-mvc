package ru.nstu.blackjack.controller;

import android.content.Context;
import android.content.SharedPreferences;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.nstu.blackjack.model.GameData;
import ru.nstu.blackjack.model.data.Deck;
import ru.nstu.blackjack.model.data.GameOutcomeStatus;
import ru.nstu.blackjack.model.data.GameState;
import ru.nstu.blackjack.model.data.GameStatus;
import ru.nstu.blackjack.model.data.PlayerState;
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

    private CompositeDisposable disposable;

    private long pendingBet = 0;

    public GameController(GameActivity view) {
        this.view = view;
        this.settings = view.getPreferences(Context.MODE_PRIVATE);
        startNewGame();
    }

    private void startNewGame() {
        this.pendingBet = 0;
        this.game = new GameData(
                settings.getLong("getMyMoney", START_MONEY),
                new Deck(interactor.getCardStack(152))
        );

        // Подписываемся на изменения модели данных игрока "Я" и отслеживаем его статус игры, чтобы узнать когда кончатся деньги
        Disposable noMoney = game.getMe()
                .getObservable()
                .map(PlayerState::getStatus)
                .filter(status -> status == GameStatus.BETTING && game.getMe().getMoney() <= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> view.showResetGameDialog(START_MONEY));

        // Подписываемся на изменения модели данных игры
        Disposable dealerHands = game.getObservable()
                .map(GameState::getDealerCards) // берем только карты диллера
                .distinctUntilChanged() // берем только новые значения, чтобы избежать перерисовки UI
                .observeOn(AndroidSchedulers.mainThread()) // выносим работу с UI в главный поток из-за ограничений ОС Android
                .subscribe(view::showDealerCards); // вызываем метод отображения карт диллера у view

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
                    view.showGameStatus(status);

                    if (status == GameStatus.SHOWDOWN) {
                        GameOutcomeStatus outcome = interactor.outcome(game.getMe(), game.getDealer());
                        view.setShowdownText(
                                outcome,
                                game.getMe().getBet(),
                                interactor.calculateWinnings(outcome, game.getMe().getBet())
                        );
                    }
                });

        disposable = new CompositeDisposable(dealerHands, monies, playerHands, bets, statuses, noMoney);

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
        startNewGame();
    }

    public void onClickBet() {
        game.getMe().initialBet(pendingBet);
        game.getMe().takeMoney(pendingBet);

        game.getDealer().getHand().addCard(game.getDeck().nextCard());
        game.getMe().getHand().addCard(game.getDeck().nextCard());
        game.getDealer().getHand().addCard(game.getDeck().nextCard());
        game.getMe().getHand().addCard(game.getDeck().nextCard());

        if (interactor.isBlackjack(game.getMe().getHand())) {
            interactor.endHand(game, game.getMe());
        }

        if (interactor.isBlackjack(game.getDealer().getHand())) {
            interactor.endHand(game, game.getMe());
        }
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

        if (interactor.isOverscore(game.getMe())) {
            game.getMe().endHand();
            if (interactor.isGameShouldShowdown(game)) {
                game.showdown();
            }
        }
    }

    public void onClickStay() {
        interactor.endHand(game, game.getMe());
    }

    /**
     * Очищаем все подписки и сохраняем данные, когда вызовется метод жизненного цикла
     * сигнализирующий об остановке приложения
     */
    public void onDestroy() {
        settings.edit()
                .putLong("getMyMoney", game.getMe().getMoney())
                .apply();
        disposable.dispose();
    }
}
