package ru.nstu.blackjack.controller;

import android.content.Context;
import android.content.SharedPreferences;

import ru.nstu.blackjack.model.Game;
import ru.nstu.blackjack.model.Player;
import ru.nstu.blackjack.view.GameActivity;

/**
 * @author Anton Vlasov - whalemare
 * @since 2019
 */
public class GameController {

    public static int DEFAULT_MONEY = 1000;

    private final GameActivity view;
    private final SharedPreferences settings;
    public Game game;
    public Player player;

    public GameController(GameActivity view) {
        this.view = view;
        this.settings = view.getPreferences(Context.MODE_PRIVATE);
        setup();
    }

    private void setup() {
        this.game = new Game();
        game.setMoney(settings.getLong("money", DEFAULT_MONEY));
        this.player = game.newPlayer();
        view.startGame(game, player);
    }


    public void onDestroy() {
        settings.edit()
                .putLong("money", game.money())
                .apply();
    }

    public void onClickPlayAgain() {
        game.resetForNewHand();
        setup();
    }

    public void onClickBet(long cost) {
        player.initialBet(cost);
    }
}
