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
import io.reactivex.disposables.Disposable;
import ru.nstu.blackjack.R;
import ru.nstu.blackjack.controller.GameController;
import ru.nstu.blackjack.model.Card;
import ru.nstu.blackjack.model.Game;
import ru.nstu.blackjack.model.GameStatus;
import ru.nstu.blackjack.model.Player;

public class GameActivity extends AppCompatActivity {

    private List<Disposable> disposables;

    private GameController controller;

    @BindView(R.id.text_money)
    TextView moneyTextView;

    @BindView(R.id.text_pending_bet)
    TextView textPendingBet;

    @BindView(R.id.text_bet_reminder)
    TextView bigBetView;

    @BindView(R.id.text_player_score)
    TextView playerScoreTextView;
    @BindView(R.id.text_dealer_score)
    TextView dealerScoreTextView;
    @BindView(R.id.text_showdown_description)
    TextView handOverTextView;

    @BindView(R.id.button_bet)
    Button buttonMakeBet;

    @BindView(R.id.button_increment_bet)
    Button buttonIncrementBet;
    @BindView(R.id.button_decrement_bet)
    Button buttonDecrementBet;

    @BindView(R.id.layout_bet_decision)
    View containerBetting;

    @BindView(R.id.layout_hitting_decision)
    View containerHitting;

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
        buttonIncrementBet.setEnabled(money > 100);
    }

    public void showPlayers(List<Player> players) {
        Log.d("GAME", "showPlayers: " + players.toString());
    }

    public void showResetGameDialog(final int dollars) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle("Закончились деньги")
                .setMessage(String.format(Locale.US,
                        "Но вы можете попробовать снова, начав с $%d", dollars))
                .setPositiveButton("Начать", (dialog, which) -> controller.onClickResetGame())
                .show();
    }


    @OnClick(R.id.button_decrement_bet)
    public void decrementBet() {
        controller.onClickDecrementBet();
    }

    @OnClick(R.id.button_increment_bet)
    public void incrementBet() {
        controller.onClickIncrementBet();
    }

    @OnClick(R.id.button_bet)
    public void onBet() {
        controller.onClickBet();
    }

    public void showBet(long bet) {
        bigBetView.setText(getString(R.string.current_bet, bet));
        textPendingBet.setText(getString(R.string.current_bet, bet));
    }

    @OnClick(R.id.button_hit)
    public void onHit() {
        controller.onClickHit();
    }

    @OnClick(R.id.button_stay)
    public void onStay() {
        controller.onClickStay();
    }

    void setShowdownText(Game game) {
        Resources resources = getResources();
        long winnings = game.getMe().winnings();

        String text;
        switch (game.getMe().outcome()) {
            case PUSH:
                handOverTextView.setText(R.string.push);
                break;
            case PLAYER_BLACKJACK:
                text = String.format(resources.getString(R.string.player_blackjack), winnings - game.getMe().getBet());
                handOverTextView.setText(text);
                break;
            case DEALER_BLACKJACK:
                text = String.format(resources.getString(R.string.dealer_blackjack), game.getMe().getBet());
                handOverTextView.setText(text);
                break;
            case PLAYER_WIN:
                text = String.format(resources.getString(R.string.player_wins), winnings - game.getMe().getBet());
                handOverTextView.setText(text);
                break;
            case DEALER_BUST:
                text = String.format(resources.getString(R.string.dealer_busts), winnings - game.getMe().getBet());
                handOverTextView.setText(text);
                break;
            case DEALER_WIN:
                text = String.format(resources.getString(R.string.dealer_wins), game.getMe().getBet());
                handOverTextView.setText(text);
                break;
            case PLAYER_BUST:
                text = String.format(resources.getString(R.string.player_busts), game.getMe().getBet());
                handOverTextView.setText(text);
                break;
            case ERROR:
                handOverTextView.setText(R.string.hand_outcome_error);
                break;
        }
    }

    public void showDealerCards(List<Card> cards) {
        TransitionManager.beginDelayedTransition(dealerHandView, transitionSet);
        if (controller.game.getMe().status() == GameStatus.BETTING) {
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


    public void showPlayerCards(List<Card> cards) {
        TransitionManager.beginDelayedTransition(playerHandView, transitionSet);

        if (controller.game.getMe().status() == GameStatus.BETTING) {
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
        playerScoreTextView.setText(String.valueOf(controller.game.getMe().score()));
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

    private void showMoneyChange(double change) {
        if (change > 0) {
            moneyTextView.setText(String.format("%s\n+ %s", moneyTextView.getText(), currencyFormat.format(change)));
        }
    }


    @OnClick(R.id.button_play_again)
    public void playAgain() {
        controller.onClickOneMoreGame();
    }

    public void showGameStatus(GameStatus status, Game game) {
        if (status == GameStatus.BETTING) {
            containerHitting.setVisibility(View.GONE);
            waitingView.setVisibility(View.GONE);
            playAgainView.setVisibility(View.GONE);
            containerBetting.setVisibility(View.VISIBLE);
        } else if (status == GameStatus.HITTING) {
            containerBetting.setVisibility(View.GONE);
            waitingView.setVisibility(View.GONE);
            playAgainView.setVisibility(View.GONE);
            containerHitting.setVisibility(View.VISIBLE);
        } else if (status == GameStatus.WAITING) {
            containerBetting.setVisibility(View.GONE);
            containerHitting.setVisibility(View.GONE);
            playAgainView.setVisibility(View.GONE);
            waitingView.setVisibility(View.VISIBLE);
        } else if (status == GameStatus.SHOWDOWN) {
            setShowdownText(game);
            containerBetting.setVisibility(View.GONE);
            containerHitting.setVisibility(View.GONE);
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

    public void enableIncrementButton(boolean canIncrement) {
        buttonIncrementBet.setEnabled(canIncrement);
    }

    public void enableDecrementButton(boolean canDecrement) {
        buttonDecrementBet.setEnabled(canDecrement);
    }

    public void enableMakeBetButton(boolean canMakeBet) {
        buttonMakeBet.setEnabled(canMakeBet);
    }

}