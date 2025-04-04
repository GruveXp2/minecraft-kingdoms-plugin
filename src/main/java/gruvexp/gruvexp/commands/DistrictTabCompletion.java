package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DistrictTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return List.of();}
        Kingdom kingdom = KingdomsManager.getSelectedKingdom(p);

        if (args.length == 1) return new ArrayList<>(kingdom.getDistrictIDs());
        District district = kingdom.getDistrict(args[0]);
        if (district == null) return List.of(ChatColor.RED + "Unknown district: " + args[0]);

        if (args.length == 2) return List.of("info", "set icon", "add", "remove");
        String oper = args[1];
        switch (oper) {
            case "set" -> {
                if (args.length == 3) return List.of("icon");
                if (args.length == 4) return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[3])).collect(Collectors.toList());
            }
            case "add" -> {
                if (args.length == 3) return List.of("locality", "rail_section");
                String feature = args[2];
                switch (feature) {
                    case "locality" -> {
                        if (args.length == 4) return List.of("<name>");
                        if (args.length == 5) return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[3])).collect(Collectors.toList());
                    }
                    case "rail_section" -> {
                        if (args.length == 4) return List.of("<name>");
                    }
                    default -> {
                        return List.of(ChatColor.RED + "unknown property: " + feature);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 3) return List.of("locality", "rail_section");
                String feature = args[2];
                if (args.length > 4) break;
                switch (feature) {
                    case "locality" -> {
                        return new ArrayList<>(district.getLocalityIDs());
                    }
                    case "rail_section" -> {
                        return new ArrayList<>(district.getSectionIDs());
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
