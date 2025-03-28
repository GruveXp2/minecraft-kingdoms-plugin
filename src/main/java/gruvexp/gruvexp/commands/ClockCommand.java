package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.clock.ClockManager;
import gruvexp.gruvexp.clock.IncreaseDigit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClockCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "you need 1 or more arguments");
            return true;
        }

        switch (args[0]) {
            case "show" -> {
                int x, y, z;
                try {
                    x = Integer.parseInt(args[1]);
                    y = Integer.parseInt(args[2]);
                    z = Integer.parseInt(args[3]);
                    ClockManager.clockInit(x, y, z);
                } catch (NumberFormatException e) {
                    p.sendMessage(ChatColor.RED + "Syntax error! " + ChatColor.WHITE + "Only numbers are allowed");
                    return true;
                }
                return true;
            }
            case "run" -> {
                int seconds = Integer.parseInt(args[1]);
                new IncreaseDigit(seconds).runTaskTimer(Main.getPlugin(), 0L, 20L);
                return true;
            }
            case "set" -> {
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
            }
            case null, default -> p.sendMessage(ChatColor.RED + args[0] + "is not a valid argument!");
        }


        return true;
    }
}
