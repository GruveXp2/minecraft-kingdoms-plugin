package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KingdomTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return new ArrayList<>(KingdomsManager.getKingdomIDs());
        Kingdom kingdom = KingdomsManager.getKingdom(args[0]);
        if (kingdom == null) return List.of(ChatColor.RED + "Unknown kingdom: " + args[0]);

        if (args.length == 2) return List.of("info", "set", "add", "remove");
        String oper = args[1];
        switch (oper) {
            case "set" -> {
                if (args.length == 3) return List.of("king", "color");
                String property = args[2];
                switch (property) {
                    case "color" -> {
                        if (args.length == 4) {
                            String hex = args[3];
                            if (!hex.contains("#")) return List.of("#", "#rrggbb");
                            return List.of(hex, "#rrggbb");
                        }
                    }
                    case "king" -> {
                        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
                    }
                    default -> {
                        return List.of(ChatColor.RED + "unknown property: " + property);
                    }
                }
            }
            case "add" -> {
                if (args.length == 3) return List.of("district", "citizen");
                String feature = args[2];
                switch (feature) {
                    case "district" -> {
                        if (args.length == 4) return List.of("<name>");
                        if (args.length == 5) return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[3])).collect(Collectors.toList());
                    }
                    case "citizen" -> {
                        if (args.length == 4) return List.of("<name>");
                        if (args.length == 5) return Registry.VILLAGER_TYPE.stream().map(t -> t.toString().toLowerCase()).filter(t -> t.contains(args[3])).collect(Collectors.toList());
                        if (args.length == 6) return Registry.VILLAGER_PROFESSION.stream().map(t -> t.toString().toLowerCase()).filter(t -> t.contains(args[3])).collect(Collectors.toList());
                    }
                    default -> {
                        return List.of(ChatColor.RED + "unknown property: " + feature);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 3) return List.of("district", "citizen");
                String feature = args[2];
                switch (feature) {
                    case "district" -> {
                        return new ArrayList<>(kingdom.getDistrictIDs());
                    }
                    case "citizen" -> {
                        return new ArrayList<>(kingdom.getCitizenNames());
                    }
                    default -> {
                        return List.of(ChatColor.RED + "unknown property: " + feature);
                    }
                }
            }
        }
        return List.of();
    }
}
