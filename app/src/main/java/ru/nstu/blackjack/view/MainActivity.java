package ru.nstu.blackjack.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import ru.nstu.blackjack.R;
import ru.nstu.blackjack.controller.MainController;

public class MainActivity extends AppCompatActivity {

    private final MainController controller = new MainController(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button playButton = findViewById(R.id.button_play);
        playButton.setOnClickListener(v -> controller.onClickPlay());
    }

    public void routeToGame() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
        this.finish();
    }
}
