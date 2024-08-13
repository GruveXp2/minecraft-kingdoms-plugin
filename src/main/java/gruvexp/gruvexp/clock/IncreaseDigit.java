package gruvexp.gruvexp.clock;

import gruvexp.gruvexp.clock.ClockManager;
import org.bukkit.scheduler.BukkitRunnable;

public class IncreaseDigit extends BukkitRunnable {
    int time = 0;
    int max;

    public IncreaseDigit(int seconds) {
        max = seconds;
    }
    @Override
    public void run() {
        time ++;
        ClockManager.increaseTime();
        if (time >= max) {cancel();}
    }
}
