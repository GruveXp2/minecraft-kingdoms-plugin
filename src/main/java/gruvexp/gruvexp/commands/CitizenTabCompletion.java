package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CitizenTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 1) {
            return new ArrayList<>(KingdomsManager.getKingdomIDs());
        }
        try {
            String kingdomID = args[0];
            Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
            if (args.length == 2) {
                return List.of("add", "get", "list", "set", "remove", "tp");
            }
            String oper = args[1];
            if (args.length == 3) {
                switch (oper) {
                    case "add":
                        return List.of("<name>");
                    case "get":
                    case "set":
                    case "remove":
                    case "tp":
                        return new ArrayList<>(kingdom.getCitizenNames());
                }
            }
            switch (oper) {
                case "add" -> {
                    if (args.length == 4) {
                        return Arrays.stream(Villager.Type.values()).map(Enum::toString).map(String::toLowerCase).collect(Collectors.toList());
                    } else if (args.length == 5) {
                        return Arrays.stream(Villager.Profession.values()).map(Enum::toString).map(String::toLowerCase).collect(Collectors.toList());
                    } else if (args.length == 6) {
                        return new ArrayList<>(kingdom.getDistrictIDs());
                    }
                    District district = kingdom.getDistrict(args[5]);
                    if (args.length == 7) {
                        return new ArrayList<>(district.getAddressIDs());
                    }
                    Address address = district.getAddress(args[6]);
                    if (args.length == 8) {
                        return address.getHouseIDs().stream().map(Object::toString).collect(Collectors.toList());
                    }
                }
                case "get" -> {
                    return List.of("bio");
                }
                case "set" -> {
                    if (args.length == 4) {
                        return List.of("home_address", "work_address", "bio");
                    }
                    String property = args[3];
                    switch (property) {
                        case "home_address" -> {
                            if (args.length == 5) {
                                return new ArrayList<>(kingdom.getDistrictIDs());
                            }
                            District district = kingdom.getDistrict(args[5]);
                            if (args.length == 6) {
                                return new ArrayList<>(district.getAddressIDs());
                            }
                            Address address = district.getAddress(args[6]);
                            if (args.length == 7) {
                                return address.getHouseIDs().stream().map(Object::toString).collect(Collectors.toList());
                            }
                        }
                        case "work_address" -> {
                            if (args.length == 5) {
                                return new ArrayList<>(KingdomsManager.getKingdomIDs());
                            }
                            Kingdom workKingdom = KingdomsManager.getKingdom(args[4]);
                            if (args.length == 6) {
                                return new ArrayList<>(workKingdom.getDistrictIDs());
                            }
                            District district = workKingdom.getDistrict(args[5]);
                            if (args.length == 7) {
                                return new ArrayList<>(district.getAddressIDs());
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            return List.of(e.getMessage());
        }
        return new ArrayList<>(0);
    }
}
