package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class LoadCitizensCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        KingdomsManager.loadCitizens(args.length == 1 && args[0].equals("respawn"));

        return true;
    }
}
