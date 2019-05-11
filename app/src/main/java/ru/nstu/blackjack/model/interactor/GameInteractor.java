package ru.nstu.blackjack.model.interactor;

/**
 * @author Anton Vlasov - whalemare
 * @since 2019
 */
public class GameInteractor {

    private static final int BET_STEP = 100;

    public long decrementBet(long currentBet) {
        return Math.max(0, currentBet - BET_STEP);
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
}
