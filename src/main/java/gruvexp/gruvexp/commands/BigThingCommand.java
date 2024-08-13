package gruvexp.gruvexp.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BigThingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {return false;}
        Player p = (Player) sender; // -3907 94 908
        p.teleport(new Location(Bukkit.getWorld("Four Kingdoms"), -3907, 94, 908, 180, -10));
        return true;
    }
}
