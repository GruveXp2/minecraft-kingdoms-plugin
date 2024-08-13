package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.clock.ClockManager;
import gruvexp.gruvexp.clock.IncreaseDigit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class ClockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "you need 1 or more arguments");
            return true;
        }

        if (Objects.equals(args[0], "show")) {
            ClockManager.clockInit(-3319, 121, 2255);
            return true;
        } else if (Objects.equals(args[0], "run")) {
            int seconds = Integer.parseInt(args[1]);
            new IncreaseDigit(seconds).runTaskTimer(Main.getPlugin(), 0L, 20L);
            return true;
        } else if (Objects.equals(args[0], "set")) {
            if (args.length < 4) {
                p.sendMessage(ChatColor.RED + "Syntax error! " + ChatColor.WHITE + "Usage: /digitalclock set <hours> <minutes> <seconds>");
                return true;
            }
            int hr, min, sec;

            try {
                hr = Integer.parseInt(args[1]);
                min = Integer.parseInt(args[2]);
                sec = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                p.sendMessage(ChatColor.RED + "Syntax error! " + ChatColor.WHITE + "Only numbers are allowed");
                return true;
            }
            ClockManager.setTime(hr, min, sec);
        } else {
            p.sendMessage(ChatColor.RED + args[0] + "is not a valid argument!");
        }


        return true;
    }
}
