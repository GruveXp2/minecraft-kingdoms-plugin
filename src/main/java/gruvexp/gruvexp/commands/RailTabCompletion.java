package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RailTabCompletion implements TabCompleter {


    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (!(sender instanceof Player p)) {return List.of();}
        District district = KingdomsManager.getSelectedDistrict(p);
        if (district == null) return List.of(ChatColor.RED + "You must select a district to work with!", "run /kingdoms select <kingdom> <district>");

        if (args.length == 1) return district.getSectionIDs().stream().toList();
        Section section = district.getSection(args[0]);
        if (section == null) return List.of(ChatColor.RED + "Unknown rail section: " + args[0]);

        if (args.length == 2) return List.of("info", "view", "set", "calculate_length", "remove");

        String oper = args[1];
        switch (oper) {
            case "set" -> {
                if (args.length == 3) return List.of("entry", "exit", "speed", "next_section", "route", "border");
                String property = args[2];
                switch (property) {
                    case "entry", "exit" -> {
                        Coord coord = Utils.getPlayerBlockCoords(p);
                        return List.of(coord.toString());
                    }
                    case "speed" -> {
                        if (args.length == 4) return List.of("normal", "fast", "express");
                    }
                    case "next_section" -> {
                        if (section.hasBorder()) {
                            district = section.getBorder();
                        }
                        if (args.length == 4) return district.getSectionIDs().stream().toList();
                    }
                    case "route" -> {
                        if (args.length == 4) return KingdomsManager.DIRECTIONS.stream().toList();
                        if (args.length == 5) return KingdomsManager.RAIL_SHAPES.stream().toList();
                        if (section.hasBorder()) {
                            district = section.getBorder();
                        }
                        if (args.length == 6) return district.getSectionIDs().stream().toList();

                        ArrayList<String> out = new ArrayList<>();
                        out.addAll(district.getLocalityIDs());
                        out.addAll(district.getKingdom().getDistrictIDs());
                        out.addAll(KingdomsManager.getKingdomIDs());
                        return out;
                    }
                    case "border" -> {
                        if (args.length == 4) return KingdomsManager.getKingdomIDs().stream().toList();
                        String kingdomID = args[3];
                        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                        if (kingdom == null) return List.of(ChatColor.RED + "Kingdom \"" + kingdomID + "\" doesnt exist!");

                        if (args.length == 5) return kingdom.getDistrictIDs().stream().toList();
                    }
                    default -> {
                        return List.of(ChatColor.RED + "\"" + property + "\" is not a settable property!");
                    }
                }
            }
            case "remove" -> {
                if (args.length == 3) return List.of("route", "border");
                String property = args[2];
                if (property.equals("route")) {
                    if (args.length == 4) {
                        List<String> out = new ArrayList<>(KingdomsManager.DIRECTIONS);
                        out.add("all");
                        return out;
                    }
                }
            }
            case "calculate_length" -> {
                if (args.length == 4) return KingdomsManager.DIRECTIONS.stream().toList();
            }
        }
        return List.of();
    }
}
