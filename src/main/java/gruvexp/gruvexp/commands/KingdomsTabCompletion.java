package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KingdomsTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("info", "add", "remove");
        String oper = args[0];
        switch (oper) {
            case "add", "remove" -> {
                return new ArrayList<>(KingdomsManager.getKingdomIDs());
            }
        }
        return List.of();
    }
}
