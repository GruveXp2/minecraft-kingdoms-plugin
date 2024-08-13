package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.Section;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RailTabCompletion implements TabCompleter {


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        Player p = (Player) sender;

        if (args.length == 1) {
            return List.of("add", "get", "set", "remove", "calculate_length", "entrypoint", "addresses");
        }
        String oper = args[0];
        String kingdomID;
        Kingdom kingdom = null;
        String districtID;
        District district = null;
        String sectionID;
        Section section;
        if (!Objects.equals(oper, "addresses") && !Objects.equals(oper, "entrypoint")) {
            if (args.length == 2) {
                return new ArrayList<>(KingdomsManager.getKingdomIDs()); // returnerer kingdoms (step 1 av å velge sektor)
            }
            kingdomID = args[1];
            try {
                kingdom = KingdomsManager.getKingdom(kingdomID);
                if (args.length == 3) {
                    return new ArrayList<>(kingdom.getDistrictIDs()); // returnerer distrikter (step 2 av å velge sektor)
                }
                districtID = args[2];
                district = kingdom.getDistrict(districtID);
                if (!Objects.equals(oper, "entrypoint")) {
                    if (args.length == 4) {
                        return new ArrayList<>(district.getSectionIDs()); // returnerer sektorer (step 3 av å velge sektor)
                    }
                }

            } catch (IllegalArgumentException e) {
                return new ArrayList<>(0);
            }
        }
        switch (oper) {
            case "get":
                assert district != null;
                if (args.length == 5) {
                    sectionID = args[3];
                    try {
                        section = district.getSection(sectionID);
                    } catch (IllegalArgumentException e) {
                        return new ArrayList<>(0);
                    }
                    return section.getProperties();
                }
                return null;
            case "set": // rail set <section> route right east_west <target_section>
                assert district != null;
                if (args.length == 5) {
                    return List.of("entry", "exit", "route", "monoroute", "border", "speed", "name");
                }
                String property = args[4];
                switch (property) {
                    case "entry", "exit":
                        Coord coord = Utils.getPlayerBlockCoords(p);
                        return new ArrayList<>(List.of(coord.getX() + " " + coord.getY()  + " " + coord.getZ()));
                    case "route": // /rail set jungle central M1_e route
                        sectionID = args[3];
                        section = district.getSection(sectionID);
                        if (section.hasBorder()) {
                            String borderKingdomID = section.getBorderKingdom();
                            if (borderKingdomID != null) {
                                kingdom = KingdomsManager.getKingdom(borderKingdomID);
                            }
                            district = kingdom.getDistrict(section.getBorderDistrict());
                        }

                        if (args.length == 6) {
                            return new ArrayList<>(KingdomsManager.ROUTES);
                        } else if (args.length == 7) {
                            return new ArrayList<>(KingdomsManager.RAIL_SHAPES);
                        } else if (args.length == 8) {
                            return new ArrayList<>(district.getSectionIDs());
                        } else {
                            ArrayList<String> out = new ArrayList<>(7);
                            out.addAll(district.getAddressIDs());
                            out.addAll(kingdom.getDistrictIDs());
                            out.addAll(KingdomsManager.getKingdomIDs());
                            return out;
                        }
                    case "monoroute":
                        sectionID = args[3];
                        try {
                            section = district.getSection(sectionID);
                        } catch (IllegalArgumentException e) {
                            return new ArrayList<>(0);
                        }
                        if (section.hasBorder()) {
                            String borderKingdomID = section.getBorderKingdom();
                            if (borderKingdomID != null) {
                                kingdom = KingdomsManager.getKingdom(borderKingdomID);
                            }
                            district = kingdom.getDistrict(section.getBorderDistrict());
                        }
                        if (args.length == 6) {
                            return new ArrayList<>(district.getSectionIDs());
                        }
                        break;
                    case "speed":
                        if (args.length == 6) {
                            return List.of("normal", "fast", "express");
                        }
                        break;
                    case "border":
                        if (args.length == 6) {
                            ArrayList<String> out = new ArrayList<>();
                            out.addAll(kingdom.getDistrictIDs());
                            out.addAll(KingdomsManager.getKingdomIDs());
                            return out;
                        }
                        if (args.length == 7) {
                            try {
                                kingdom = KingdomsManager.getKingdom(args[5]);
                                return new ArrayList<>(kingdom.getDistrictIDs());
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        }
                }
            case "remove":
                if (args.length == 5) {
                    return List.of("route", "monoroute", "border");
                }
                if (Objects.equals(args[4], "route")) {
                    return List.of("all", "forward", "right", "left", "forward_roundabout", "right_roundabout", "left_roundabout", "u_turn");
                }
                break;
            case "calculate_length":
                if (args.length == 5) {
                    return List.of("n", "s", "e", "w");
                }
                break;
            case "entrypoint": { // entrypoint add
                //       /rail entrypoint add   pyralix   central western_hills western_hills_n e
                //                 0       1       2         3          4              5        6
                if (args.length == 2) {
                    return List.of("add", "get", "remove");
                }
                String oper2 = args[1];
                if (args.length == 3) {
                    return new ArrayList<>(KingdomsManager.getKingdomIDs());
                }
                kingdomID = args[2];
                districtID = args[3];
                try {
                    kingdom = KingdomsManager.getKingdom(kingdomID);
                    if (args.length == 4) {
                        return new ArrayList<>(kingdom.getDistrictIDs());
                    }
                    district = kingdom.getDistrict(districtID);
                    if (args.length == 5) {
                        return new ArrayList<>(district.getAddressIDs());
                    }
                    if (args.length == 6 && !district.getAddressIDs().contains(args[4])) {
                        return null;
                    }
                    if (!Objects.equals(oper2, "add")) {
                        return null;
                    }
                    if (args.length == 6) {
                        return new ArrayList<>(district.getSectionIDs());
                    }
                    if (args.length == 7) {
                        return new ArrayList<>(KingdomsManager.DIRECTIONS);
                    }
                }
                catch (IllegalArgumentException e) {
                    return new ArrayList<>(0);
                }
                break;
            }
            case "addresses": // /rail addresses add <kingdom> <district> <address> <material>
                if (args.length == 2) {
                    return List.of("add", "get");
                }
                if (args.length == 3) {
                    return new ArrayList<>(KingdomsManager.getKingdomIDs());
                }
                try {
                    kingdom = KingdomsManager.getKingdom(args[2]);
                } catch (IllegalArgumentException e) {
                    return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[3])).collect(Collectors.toList());
                }
                if (args.length == 4) {
                    return new ArrayList<>(kingdom.getDistrictIDs());
                }
                try {
                    district = kingdom.getDistrict(args[3]);
                } catch (IllegalArgumentException e) {
                    return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[4])).collect(Collectors.toList());
                }
                if (args.length == 5) {
                    return new ArrayList<>(district.getAddressIDs());
                }
                if (args.length == 6 && !district.getAddressIDs().contains(args[4])) {
                    return KingdomsManager.BLOCKS.stream().filter(b -> b.contains(args[5])).collect(Collectors.toList());
                }
                break;
            default:
                break;
        }
        return new ArrayList<>(0);
    }
}
