package gruvexp.gruvexp.clock;

import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;

import java.util.ArrayList;

public class Number {

// if its 1 digit only: MOVE 1 PIXEL UP AND -2 PIXELS X !
    private final ArrayList<Digit> digits = new ArrayList<>(2);
    private final int max;
    private int number = -1;
    private final int id;

    public Number(int x, int y, int z, int max, int id) {
        this.max = max;
        this.id = id;

        for (int i = 0; i < String.valueOf(max).length(); i++) {
            Digit digit = new Digit(x - 0.125 + 3*i, y + 0.0625, z);
            // husk å vite hvor som er enerplass og hvor som er tierplass
            digits.add(digit);
        }
        setNumber(0);
    }

    public void setNumber(int number) {
        if (number >= max) {return;}
        if (number == this.number) {return;}

        this.number = number;
        String num = String.valueOf(number);
        while (num.length() < digits.size()) {
            num = "0" + num;
        }

        for (int j = 0; j < digits.size(); j++) {
            int digit = Integer.parseInt(String.valueOf(num.charAt(num.length() - j - 1)));
            digits.get(j).setDigit(digit);
        }
    }

    public void increaseNumber() {
        number ++;
        if (number == max) {
            number = 0;
            for (Digit digit : digits) {
                digit.setDigit(0);
            }
            ClockManager.increaseNumber(id + 1);
            return;
        }
        digits.get(0).increaseDigit();
        carry(1); // skjekker om det skal carries over eller ikke

    }

    private void carry(int i) {
        if (digits.size() > i && (number % Math.pow(10, i)) == 0) {
            digits.get(i).increaseDigit();
            carry(i + 1);
        }
    }

    public void delete() {
        for (Digit digit : digits) {
            digit.delete();
        }
    }

}
