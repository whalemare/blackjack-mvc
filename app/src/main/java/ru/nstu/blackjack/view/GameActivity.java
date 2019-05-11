package ru.nstu.blackjack.view;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import ru.nstu.blackjack.R;
import ru.nstu.blackjack.controller.GameController;
import ru.nstu.blackjack.model.Card;
import ru.nstu.blackjack.model.Game;
import ru.nstu.blackjack.model.GameState;
import ru.nstu.blackjack.model.GameStatus;
import ru.nstu.blackjack.model.Player;
import ru.nstu.blackjack.model.PlayerState;

public class GameActivity extends AppCompatActivity {

    private List<Disposable> disposables;

    private GameController controller;

    @BindView(R.id.text_money)
    TextView moneyTextView;
    @BindView(R.id.text_bet)
    TextView betTextView;
    @BindView(R.id.text_player_score)
    TextView playerScoreTextView;
    @BindView(R.id.text_dealer_score)
    TextView dealerScoreTextView;
    @BindView(R.id.text_showdown_description)
    TextView handOverTextView;
    @BindView(R.id.text_bet_reminder)
    TextView bigBetView;
    @BindView(R.id.button_bet)
    Button betButton;
    @BindView(R.id.button_increment_bet)
    Button incrementBetButton;
    @BindView(R.id.button_decrement_bet)
    Button decrementBetButton;
    @BindView(R.id.button_double)
    Button doubleButton;
    @BindView(R.id.button_split)
    Button splitButton;
    @BindView(R.id.layout_bet_decision)
    View betDecisionView;
    @BindView(R.id.layout_hitting_decision)
    View hitAndStayView;
    @BindView(R.id.layout_play_again)
    View playAgainView;
    @BindView(R.id.layout_waiting)
    View waitingView;
    @BindView(R.id.layout_dealer_hand)
    LinearLayout dealerHandView;
    @BindView(R.id.layout_player_hand)
    LinearLayout playerHandView;

    private NumberFormat currencyFormat;

    private Unbinder unbinder;
    private CompositeDisposable disposable;
    private TransitionSet transitionSet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        unbinder = ButterKnife.bind(this, this);
        if (savedInstanceState == null) {
            controller = new GameController(this);

            transitionSet = new TransitionSet()
                    .setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                    .addTransition(new TransitionSet()
                            .setOrdering(TransitionSet.ORDERING_TOGETHER)
//                            .addTransition(new CardFlip())
                            .addTransition(new ChangeBounds()))
                    .addTransition(new Slide(Gravity.END));
            currencyFormat = NumberFormat.getCurrencyInstance();
            currencyFormat.setMaximumFractionDigits(0);
        }
    }

    public void showMoney(long money) {
        moneyTextView.setText(getString(R.string.your_money, money));
        incrementBetButton.setEnabled(money > 100);
    }

    public void startGame(Game game, Player player) {
        disposables = new ArrayList<>();
        Disposable listsOfPlayers = game.getObservable()
                .map(GameState::getPlayerCount)
                .distinctUntilChanged()
                .map(count -> game.players())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showPlayers);

        Disposable noMoney = game.players().get(0).getObservable()
                .map(PlayerState::getStatus)
                .filter(status -> status == GameStatus.BETTING && game.money() <= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> showMoneyDialog(1000));

        Disposable dealerHands = game.getObservable()
                .map(GameState::getDealerCards)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showDealerCards);

        Disposable monies = game.getObservable()
                .map(GameState::getMoney)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showMoney);
        Disposable playerHands = player.getObservable()
                .map(PlayerState::getCards)
                .distinctUntilChanged()
                .filter(cards -> cards.size() != 1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showPlayerCards);
        Disposable bets = player.getObservable()
                .map(PlayerState::getBet)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showBet);
        Disposable statuses = player.getObservable()
                .map(PlayerState::getStatus)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showDecisionView);

        disposable = new CompositeDisposable(dealerHands, monies, playerHands, bets, statuses);

        Collections.addAll(disposables, listsOfPlayers, noMoney);
        showMoney(game.money());
    }

    private void showPlayers(List<Player> players) {
        Log.d("GAME", "showPlayers: " + players.toString());
    }

    private void showMoneyDialog(final int dollars) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle("Need more money?")
                .setMessage(String.format(Locale.US,
                        "Press OK to start over with another $%d", dollars))
                .setPositiveButton("OK", (dialog, which) -> controller.game.setMoney(dollars))
                .show();
    }

    private long getPendingBet() {
        String text = betTextView.getText().toString();
        return Long.parseLong(text.substring(1, text.length()));
    }

    private void setPendingBet(long bet) {
        betTextView.setText(getString(R.string.current_bet, bet));

        long decrementAmount;
        if (bet < 100 && bet != 0) {
            decrementAmount = bet;
        } else {
            decrementAmount = 100;
        }
        decrementBetButton.setText(getString(R.string.decrement_bet, decrementAmount));
        decrementBetButton.setEnabled(bet != 0);
        betButton.setEnabled(bet != 0);

        long incrementAmount;
        if (bet > controller.game.money() - 100 && bet != controller.game.money()) {
            incrementAmount = bet;
        } else {
            incrementAmount = 100;
        }
        incrementBetButton.setText(getString(R.string.increment_bet, incrementAmount));
        incrementBetButton.setEnabled(bet != controller.game.money());
    }

    @OnClick(R.id.button_decrement_bet)
    public void decrementBet() {
        long betDecrease;
        if (getPendingBet() < 100) {
            betDecrease = getPendingBet();
        } else {
            betDecrease = 100;
        }

        setPendingBet(getPendingBet() - betDecrease);
    }

    @OnClick(R.id.button_increment_bet)
    public void incrementBet() {
        long newBet;
        if (controller.game.money() < 100 + getPendingBet()) {
            newBet = controller.game.money();
        } else {
            newBet = getPendingBet() + 100;
        }

        setPendingBet(newBet);
    }

    @OnClick(R.id.button_bet)
    public void onBet() {
        controller.onClickBet(getPendingBet());
    }

    private void showBet(long bet) {
        bigBetView.setText(getString(R.string.current_bet, bet));
    }

    @OnClick(R.id.button_hit)
    public void onHit() {
        controller.player.hit();
    }

    @OnClick(R.id.button_stay)
    public void onStay() {
        controller.player.stay();
    }

    @OnClick(R.id.button_double)
    public void onDouble() {
        controller.player.doubleHand();
    }

    @OnClick(R.id.button_split)
    public void onSplit() {
        controller.player.split();
    }

    void setShowdownText() {
        Resources resources = getResources();
        long winnings = controller.player.winnings();

        String text;
        switch (controller.player.outcome()) {
            case PUSH:
                handOverTextView.setText(R.string.push);
                break;
            case PLAYER_BLACKJACK:
                text = String.format(resources.getString(R.string.player_blackjack), winnings - controller.player.getBet());
                handOverTextView.setText(text);
                break;
            case DEALER_BLACKJACK:
                text = String.format(resources.getString(R.string.dealer_blackjack), controller.player.getBet());
                handOverTextView.setText(text);
                break;
            case PLAYER_WIN:
                text = String.format(resources.getString(R.string.player_wins), winnings - controller.player.getBet());
                handOverTextView.setText(text);
                break;
            case DEALER_BUST:
                text = String.format(resources.getString(R.string.dealer_busts), winnings - controller.player.getBet());
                handOverTextView.setText(text);
                break;
            case DEALER_WIN:
                text = String.format(resources.getString(R.string.dealer_wins), controller.player.getBet());
                handOverTextView.setText(text);
                break;
            case PLAYER_BUST:
                text = String.format(resources.getString(R.string.player_busts), controller.player.getBet());
                handOverTextView.setText(text);
                break;
            case ERROR:
                handOverTextView.setText(R.string.hand_outcome_error);
                break;
        }
    }

    //endregion

    //region Hand Views

    private void showDealerCards(List<Card> cards) {
        TransitionManager.beginDelayedTransition(dealerHandView, transitionSet);
        if (controller.player.status() == GameStatus.BETTING) {
            dealerHandView.removeAllViews();
            cards = new ArrayList<>();
            cards.add(Card.dealerBlank);
            cards.add(Card.dealerBlank);
        }
        for (int i = 0; i < cards.size(); i++) {
            Card card;
            ImageView imageView;
            card = cards.get(i);
            if (i < dealerHandView.getChildCount()) {
                imageView = (ImageView) dealerHandView.getChildAt(i);
            } else {
                imageView = newImageViewForLayout(dealerHandView);
            }
            setCardForImageView(card, imageView);
        }
        dealerScoreTextView.setText(String.valueOf(controller.game.dealerScore()));
    }

    private void showPlayerCards(List<Card> cards) {
        TransitionManager.beginDelayedTransition(playerHandView, transitionSet);

        if (controller.player.status() == GameStatus.BETTING) {
            cards = new ArrayList<>();
            Collections.addAll(cards, Card.playerBlank, Card.playerBlank);
        }

        // remove any extra views
        if (playerHandView.getChildCount() > cards.size()) {
            int count = playerHandView.getChildCount() - cards.size();
            playerHandView.removeViews(cards.size(), count);
        }
        // set any existing views
        for (int i = 0; i < playerHandView.getChildCount(); i++) {
            ImageView cardImageView = (ImageView) playerHandView.getChildAt(i);
            setCardForImageView(cards.get(i), cardImageView);
        }
        // add any missing views
        for (int i = playerHandView.getChildCount(); i < cards.size(); i++) {
            setCardForImageView(cards.get(i), newImageViewForLayout(playerHandView));
        }
        for (int i = cards.size(); i < 2; i++) {
            setCardForImageView(Card.playerBlank, newImageViewForLayout(playerHandView));
        }
        playerScoreTextView.setText(String.valueOf(controller.player.score()));
        doubleButton.setEnabled(controller.player.isDoublable());
        splitButton.setEnabled(controller.player.isSplittable());
    }

    private ImageView newImageViewForLayout(LinearLayout handView) {
        ImageView cardView = (ImageView) LayoutInflater.from(handView.getContext())
                .inflate(R.layout.card_item, handView, false);

        if (handView.getChildCount() == 0) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(cardView.getLayoutParams());
            params.setMargins(0, 0, 0, 0);
            cardView.setLayoutParams(params);
        }

        handView.addView(cardView);
        return cardView;
    }

    private void setCardForImageView(Card card, ImageView imageView) {
        imageView.setImageResource(card.getImageID());
        imageView.setTag(card.toString());
    }

    //endregion

    private void showMoneyChange(double change) {
        if (change > 0) {
            moneyTextView.setText(String.format("%s\n+ %s", moneyTextView.getText(), currencyFormat.format(change)));
        }
    }


    @OnClick(R.id.button_play_again)
    public void playAgain() {
        controller.onClickPlayAgain();
    }

    private void showDecisionView(GameStatus status) {
        if (status == GameStatus.BETTING) {
            hitAndStayView.setVisibility(View.GONE);
            waitingView.setVisibility(View.GONE);
            playAgainView.setVisibility(View.GONE);
            betDecisionView.setVisibility(View.VISIBLE);
        } else if (status == GameStatus.HITTING) {
            betDecisionView.setVisibility(View.GONE);
            waitingView.setVisibility(View.GONE);
            playAgainView.setVisibility(View.GONE);
            hitAndStayView.setVisibility(View.VISIBLE);
            splitButton.setEnabled(controller.player.isSplittable());
            doubleButton.setEnabled(controller.player.isDoublable());
        } else if (status == GameStatus.WAITING) {
            betDecisionView.setVisibility(View.GONE);
            hitAndStayView.setVisibility(View.GONE);
            playAgainView.setVisibility(View.GONE);
            waitingView.setVisibility(View.VISIBLE);
        } else if (status == GameStatus.SHOWDOWN) {
            setShowdownText();
            betDecisionView.setVisibility(View.GONE);
            hitAndStayView.setVisibility(View.GONE);
            waitingView.setVisibility(View.GONE);
            playAgainView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (Disposable disposable : disposables) {
            disposable.dispose();
        }
        disposables.clear();
        controller.onDestroy();
    }
}