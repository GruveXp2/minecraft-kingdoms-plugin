package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LocalityTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return List.of();}
        Kingdom kingdom = KingdomsManager.getSelectedKingdom(p);
        District district = KingdomsManager.getSelectedDistrict(p);
        if (district == null) return List.of(ChatColor.RED + "You must select a district to work with!", "run /kingdoms select <kingdom> <district>");

        if (args.length == 1) return new ArrayList<>(district.getLocalityIDs());
        Locality locality = district.getLocality(args[0]);
        if (locality == null) return List.of(ChatColor.RED + "Unknown locality: " + args[0]);

        if (args.length == 2) return List.of("info", "set icon", "add", "remove");
        String oper = args[1];
        switch (oper) {
            case "set" -> {
                if (args.length == 3) return List.of("icon");
                if (args.length == 4) return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[3])).collect(Collectors.toList());
            }
            case "add" -> {
                if (args.length == 3) return List.of("house", "path_section");
                String feature = args[2];
                switch (feature) {
                    case "house" -> {
                        if (args.length == 4) return List.of("<house number>");
                    }
                    case "path_section" -> {
                        if (args.length == 4) return List.of("<id>");
                        Location loc = p.getLocation();
                        if (args.length == 5) return List.of(String.valueOf(loc.getBlockX()), String.valueOf(loc.getBlockY()), String.valueOf(loc.getBlockZ()));
                    }
                    default -> {
                        return List.of(ChatColor.RED + "unknown property: " + feature);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 3) return List.of("house", "path_section");
                String feature = args[2];
                if (args.length > 4) break;
                switch (feature) {
                    case "house" -> {
                        return new ArrayList<>(locality.getHouseIDs());
                    }
                    case "path_section" -> {
                        return new ArrayList<>(locality.getPathIDs());
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
