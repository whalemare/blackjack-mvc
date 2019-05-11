package ru.nstu.blackjack.view;

import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import ru.nstu.blackjack.R;
import ru.nstu.blackjack.controller.GameController;
import ru.nstu.blackjack.model.Game;
import ru.nstu.blackjack.model.GameState;
import ru.nstu.blackjack.model.GameStatus;
import ru.nstu.blackjack.model.Player;
import ru.nstu.blackjack.model.PlayerState;
import ru.nstu.blackjack.views.GameFragment;

public class GameActivity extends AppCompatActivity {

    private GameFragmentPagerAdapter adapter;
    private ViewPager pager;
    private List<Disposable> disposables;

    private GameController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        if (savedInstanceState == null) {
            controller = new GameController(this);
        }
    }

    public void startGame(Game game) {
        pager = findViewById(R.id.pager);
        adapter = new GameFragmentPagerAdapter(getSupportFragmentManager());
        adapter.add(GameFragment.newInstance(game.newPlayer()));
        pager.setAdapter(adapter);
        subscribeToGameEvents(game);
    }

    private void subscribeToGameEvents(Game game) {
        disposables = new ArrayList<>();
        Disposable listsOfPlayers = game.getObservable()
                .map(GameState::getPlayerCount)
                .distinctUntilChanged()
                .map(count -> game.players())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showPlayers);

        Disposable noMoney = game.players().get(0).getObservable()
                .map(PlayerState::getStatus)
                .filter(status -> status == GameStatus.BETTING && controller.game.money() <= 0)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> showMoneyDialog(1000));

        Collections.addAll(disposables, listsOfPlayers, noMoney);
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

    private void showPlayers(List<Player> players) {
        // remove any fragments whose players were removed
        while (adapter.fragments.size() > players.size()) {
            adapter.fragments.remove(adapter.fragments.size() - 1);
        }
        // add fragments for any new players
        while (players.size() > adapter.fragments.size()) {
            addGameFragment(GameFragment.newInstance(players.get(adapter.fragments.size())));
        }
        adapter.notifyDataSetChanged();
    }

    public void resetGame() {
        pager.setCurrentItem(0);
        int count = adapter.getCount();
        for (int i = count - 1; i >= 1; i--) {
            adapter.remove(i);
        }
        controller.game.resetForNewHand();
        adapter.notifyDataSetChanged();
    }

    private void addGameFragment(GameFragment fragment) {
        adapter.add(fragment);
    }

    private class GameFragmentPagerAdapter extends FragmentPagerAdapter {
        private final List<GameFragment> fragments;

        GameFragmentPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
            fragments = new ArrayList<>();
        }

        public void add(GameFragment fragment) {
            fragments.add(fragment);
            notifyDataSetChanged();
        }

        void remove(int index) {
            fragments.remove(index);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            if (object instanceof GameFragment) {
                int index = fragments.indexOf(object);
                if (index != -1) {
                    return index;
                }
            }
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return fragments.size();
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