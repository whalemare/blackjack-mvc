package ru.nstu.blackjack.utils;

import java.util.Arrays;
import java.util.List;

/**
 * @author Anton Vlasov - whalemare
 * @since 2019
 */
public class Utils {

    public static <T> List<T> listOf(T ... items) {
        return Arrays.asList(items);
    }

}
