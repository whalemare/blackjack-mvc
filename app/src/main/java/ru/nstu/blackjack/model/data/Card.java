package ru.nstu.blackjack.model.data;


import java.io.Serializable;
import java.util.Locale;

import ru.nstu.blackjack.R;

/**
 * Representation of a standard playing card
 * <p/>
 * Created by Bryan Capps on 8/12/15.
 */
public class Card implements Serializable {

    public enum Rank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, BLANK;
    }

    public enum Suit {
        CLUBS, DIAMONDS, HEARTS, SPADES, PLAYER, DEALER;
    }

    public static final Card dealerBlank = new Card(Rank.BLANK, Suit.DEALER);
    public static final Card playerBlank = new Card(Rank.BLANK, Suit.PLAYER);

    private final static int[] CLUBS_IDS = {R.drawable.ace_of_clubs, R.drawable.two_of_clubs, R.drawable.three_of_clubs, R.drawable.four_of_clubs, R.drawable.five_of_clubs, R.drawable.six_of_clubs, R.drawable.seven_of_clubs, R.drawable.eight_of_clubs, R.drawable.nine_of_clubs, R.drawable.ten_of_clubs, R.drawable.jack_of_clubs, R.drawable.queen_of_clubs, R.drawable.king_of_clubs};
    private final static int[] DIAMONDS_IDS = {R.drawable.ace_of_diamonds, R.drawable.two_of_diamonds, R.drawable.three_of_diamonds, R.drawable.four_of_diamonds, R.drawable.five_of_diamonds, R.drawable.six_of_diamonds, R.drawable.seven_of_diamonds, R.drawable.eight_of_diamonds, R.drawable.nine_of_diamonds, R.drawable.ten_of_diamonds, R.drawable.jack_of_diamonds, R.drawable.queen_of_diamonds, R.drawable.king_of_diamonds};
    private final static int[] HEARTS_IDS = {R.drawable.ace_of_hearts, R.drawable.two_of_hearts, R.drawable.three_of_hearts, R.drawable.four_of_hearts, R.drawable.five_of_hearts, R.drawable.six_of_hearts, R.drawable.seven_of_hearts, R.drawable.eight_of_hearts, R.drawable.nine_of_hearts, R.drawable.ten_of_hearts, R.drawable.jack_of_hearts, R.drawable.queen_of_hearts, R.drawable.king_of_hearts};
    private final static int[] SPADES_IDS = {R.drawable.ace_of_spades, R.drawable.two_of_spades, R.drawable.three_of_spades, R.drawable.four_of_spades, R.drawable.five_of_spades, R.drawable.six_of_spades, R.drawable.seven_of_spades, R.drawable.eight_of_spades, R.drawable.nine_of_spades, R.drawable.ten_of_spades, R.drawable.jack_of_spades, R.drawable.queen_of_spades, R.drawable.king_of_spades};

    private Rank rank;
    private Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public int value() {
        switch (rank) {
            case ACE:
                return 1;
            case TWO:
                return 2;
            case THREE:
                return 3;
            case FOUR:
                return 4;
            case FIVE:
                return 5;
            case SIX:
                return 6;
            case SEVEN:
                return 7;
            case EIGHT:
                return 8;
            case NINE:
                return 9;
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return 10;
            default:
                return 0;
        }
    }

    public int getImageID() {
        int[] array;
        switch (suit) {
            case CLUBS:
                array = CLUBS_IDS;
                break;
            case DIAMONDS:
                array = DIAMONDS_IDS;
                break;
            case HEARTS:
                array = HEARTS_IDS;
                break;
            case SPADES:
                array = SPADES_IDS;
                break;
            case PLAYER:
                return R.drawable.red_back;
            case DEALER:
                return R.drawable.blue_back;
            default:
                return 0;
        }
        return array[rank.ordinal()];
    }

    @Override
    public String toString() {
        String rankString = rank.toString().toLowerCase();
        String suitString = suit.toString().toLowerCase();
        return String.format(Locale.US, "%s of %s", rankString, suitString);
    }
}
