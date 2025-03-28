package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Main;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BigThingCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player p)) {return false;}
        p.teleport(new Location(Main.WORLD, -3907, 94, 908, 180, -10));
        return true;
    }
}
