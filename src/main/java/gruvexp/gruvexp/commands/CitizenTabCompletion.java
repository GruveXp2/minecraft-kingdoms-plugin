package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.*;
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

public class CitizenTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return List.of("tab completion only works for players");}

        Kingdom kingdom = KingdomsManager.getSelectedKingdom(p);
        if (args.length == 1) return new ArrayList<>(kingdom.getCitizenNames());

        if (args.length == 2) {
            return List.of("info", "set", "help", "tp");
        }
        String oper = args[1];
        if (oper.equals("set")) {
            if (args.length == 3) {
                return List.of("home_address", "work_address", "bio", "profession");
            }
            String property = args[2];
            switch (property) {
                case "profession" -> {
                    return Registry.VILLAGER_PROFESSION.stream()
                            .map(t -> t.toString().toLowerCase())
                            .filter(t -> t.contains(args[3]))
                            .collect(Collectors.toList());
                }
                case "home_address" -> {
                    if (args.length == 4) {
                        String[] address = args[3].split(":");
                        if (address.length == 1) {
                            return kingdom.getDistrictIDs().stream().toList();
                        }
                        District district = kingdom.getDistrict(address[0]);
                        if (district == null) return List.of(ChatColor.RED + "Unknown district: " + address[0]);
                        if (address.length == 2) {
                            return district.getLocalityIDs().stream().toList();
                        }
                        Locality locality = district.getLocality(address[1]);
                        if (locality == null) return List.of(ChatColor.RED + "Unknown locality: " + address[1]);
                        if (address.length == 3) {
                            return locality.getHouseIDs().stream().map(Object::toString).collect(Collectors.toList());
                        }
                    }
                }
                case "work_address" -> {
                    if (args.length == 4) {
                        String[] address = args[3].split(":");
                        if (address.length == 1) {
                            return KingdomsManager.getKingdomIDs().stream().toList();
                        }
                        Kingdom workKingdom = KingdomsManager.getKingdom(address[0]);
                        if (address.length == 2) {
                            return workKingdom.getDistrictIDs().stream().toList();
                        }
                        District district = workKingdom.getDistrict(address[1]);
                        if (district == null) return List.of(ChatColor.RED + "Unknown district: " + address[1]);
                        if (address.length == 3) {
                            return district.getLocalityIDs().stream().toList();
                        }
                    }
                }
                case "bio" -> {
                    return List.of("<write some lore about this citizen>");
                }
            }
        }
        return List.of();
    }
}
