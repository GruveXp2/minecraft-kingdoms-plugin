package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.path.Path;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = (Player) sender;

        if (args.length == 1) {
            return new ArrayList<>(KingdomsManager.getKingdomIDs());
        }
        try {
            String kingdomID = args[0];
            Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
            if (args.length == 2) {
                return new ArrayList<>(kingdom.getDistrictIDs());
            }
            String districtID = args[1];
            District district = kingdom.getDistrict(districtID);
            if (args.length == 3) {
                return new ArrayList<>(district.getAddressIDs());
            }
            String addressID = args[2];
            Address address = district.getAddress(addressID);
            if (args.length == 4) {
                return List.of("add", "get", "list", "set", "remove");
            }
            String oper = args[3];
            if (args.length == 5) {
                switch (oper) {
                    case "add":
                        return List.of("<path_id>");
                    case "get":
                    case "set":
                    case "remove":
                        return new ArrayList<>(address.getPathIDs());
                }
            }
            switch (oper) {
                case "add":
                    if (args.length == 6) {
                        return List.of(Utils.getTargetBlock(p, 10).toString());
                    } else if (args.length == 9) {
                        return new ArrayList<>(Path.DIRECTIONS);
                    } else if (args.length > 9) {
                        String arg = args[args.length - 1];
                        if (Objects.equals(arg, "")) {
                            return List.of("<index (number)>");
                        } else if (!arg.contains(":")) {
                            return List.of(arg + ":");
                        } else if (arg.charAt(arg.length() - 1) == ':') {
                            return new ArrayList<>(Path.DIRECTIONS);
                        }
                    }
                    break;
                case "set":
                    if (args.length == 6) {
                        return List.of("start_pos", "turns", "branch");
                    }
                    String property = args[5];
                    switch (property) {
                        case "start_pos":
                            return List.of(Utils.getTargetBlock(p, 10).toString());
                        case "turns":
                            if (args.length == 7) {
                                return new ArrayList<>(Path.DIRECTIONS);
                            } else {
                                String arg = args[args.length - 1];
                                if (Objects.equals(arg, "")) {
                                    return List.of("<index (number)>");
                                } else if (!arg.contains(":")) {
                                    return List.of(arg + ":");
                                } else if (arg.charAt(arg.length() - 1) == ':') {
                                    return new ArrayList<>(Path.DIRECTIONS);
                                }
                            }
                            break;
                        case "branch":
                            if (args.length == 7) {
                                return List.of("<index (number)>");
                            } else if (args.length == 8) {
                                List<String> out = new ArrayList<>(address.getPathIDs());
                                out.add("enter_rail 0");
                                return out;
                            } else if (args.length == 9) {
                                return List.of("<enter index>");
                            } else {
                                List<String> out = address.getHouseIDs().stream().map(Object::toString).collect(Collectors.toList());
                                out.add("station");
                                return out;
                            }
                    }
                    break;
            }
        } catch (IllegalArgumentException e) {
            return List.of(e.getMessage());
        }
        return new ArrayList<>(0);
    }
}
