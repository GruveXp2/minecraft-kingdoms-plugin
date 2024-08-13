package gruvexp.gruvexp.clock;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class ClockManager {

    private static ArrayList<Number> numbers = new ArrayList<>(3);

    public static void clockInit(int x, int y, int z) {
        Number seconds = new Number(x, y, z, 60, 0);
        Number minutes = new Number(x + 7, y, z, 60, 1);
        Number hours = new Number(x + 14, y, z, 24, 2);
        seconds.setNumber(0);
        minutes.setNumber(0);
        hours.setNumber(0);
        numbers.add(seconds);
        numbers.add(minutes);
        numbers.add(hours);
    }

    public static void deleteNumbers() {
        for (Number number : numbers) {
            number.delete();
        }
    }

    public static void increaseNumber(int number) {
        if (number < 3) {
            numbers.get(number).increaseNumber();
        } else {
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage("A new day has started!");
            }
        }
    }

    public static void setTime(int hours, int minutes, int seconds) {
        numbers.get(0).setNumber(seconds);
        numbers.get(1).setNumber(minutes);
        numbers.get(2).setNumber(hours);
    }

    public static void increaseTime() {
        numbers.get(0).increaseNumber();
    }

}
