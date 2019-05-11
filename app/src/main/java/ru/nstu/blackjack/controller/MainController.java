package ru.nstu.blackjack.controller;

import ru.nstu.blackjack.view.MainActivity;

/**
 * @author Anton Vlasov - whalemare
 * @since 2019
 */
public class MainController {

    private final MainActivity view;

    public MainController(MainActivity view) {
        this.view = view;
    }

    public void onClickPlay() {
        view.routeToGame();
    }

}
