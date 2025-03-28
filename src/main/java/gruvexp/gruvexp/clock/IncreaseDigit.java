package gruvexp.gruvexp.clock;

import org.bukkit.scheduler.BukkitRunnable;

public class IncreaseDigit extends BukkitRunnable {
    int time = 0;
    final int max;

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
