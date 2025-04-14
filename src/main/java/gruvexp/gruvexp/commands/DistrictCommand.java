package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DistrictCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);
        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        Kingdom kingdom = KingdomsManager.getSelectedKingdom(p);

        String districtID = args[0];
        District district = kingdom.getDistrict(districtID);
        if (district == null) return Component.text(kingdom.id + " has no district named \"" + districtID + "\"!", NamedTextColor.RED);
        if (args.length == 1) return Component.text("You must specify an operation [info | set | add | remove]", NamedTextColor.GOLD);

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                return Component.newline()
                        .append(Component.text("District ", District.LABEL_COLOR)).append(district.name())
                        .append(Component.text(" in ")).append(kingdom.name()).append(Component.text(":\n"))
                        .append(Component.text("Icon: ")).append(Component.text(district.getIcon().toString().toLowerCase(), NamedTextColor.GREEN)).appendNewline()
                        .append(Component.text(district.getLocalityIDs().size())).append(Component.text(" localities", Locality.LABEL_COLOR)).append(Component.text(": "))
                        .append(Component.text(String.join(", ", district.getLocalityIDs()), Locality.VALUE_COLOR)).appendNewline()
                        .append(Component.text(district.getSectionIDs().size(), Section.VALUE_COLOR)).append(Component.text(" rail sections", Section.LABEL_COLOR));
            }
            case "set" -> {
                if (args.length < 4) return Component.text("You must specify what to set and a value: set [king | color] <value>", NamedTextColor.GOLD);
                String property = args[2];
                if (property.equals("icon")) {
                    String itemID = args[3];
                    Material icon = Material.getMaterial(itemID.toUpperCase());
                    if (icon == null)
                        return Component.text("Item called \"" + itemID + "\" doesnt exist", NamedTextColor.RED);
                    return district.setIcon(icon);
                }
                return Component.text("Invalid property argument! Syntaxs: set icon <item>", NamedTextColor.RED);
            }
            case "add" -> {
                if (args.length == 2) return Component.text("You must specify what to add: [locality | rail_section]", NamedTextColor.GOLD);
                String feature = args[2];
                switch (feature) {
                    case "locality" -> {
                        if (args.length < 5) return Component.text("You must specify the details of the new locality: add locality <id> <item icon>", NamedTextColor.GOLD);
                        String localityID = args[3];
                        String itemID = args[4];
                        Material icon = Material.getMaterial(itemID.toUpperCase());
                        if (icon == null) return Component.text("Item called \"" + itemID + "\" doesnt exits", NamedTextColor.RED);
                        return district.addLocality(localityID, icon);
                    }
                    case "rail_section" -> {
                        if (args.length < 7) return Component.text("You must specify the entry pos of the new rail section: add rail_cestion <id> <entry: pos>", NamedTextColor.GOLD);
                        String newSectionID = args[3];
                        Coord entry;
                        try {
                            entry = new Coord(args[4], args[5], args[6]);
                        } catch (IllegalArgumentException e) {
                            return Component.text("Coordinates must be only numbers!", NamedTextColor.RED);
                        }
                        return district.addSection(newSectionID, entry);
                    }
                    default -> {
                        return Component.text("Invalid argument. Syntaks: add [locality | rail_section] <properties>", NamedTextColor.RED);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 2) return Component.text("You must specify what to remove: [locality | rail_section]", NamedTextColor.GOLD);
                String feature = args[2];
                switch (feature) {
                    case "locality" -> {
                        if (args.length == 3) return Component.text("You must specify which locality to remove: locality <id>", NamedTextColor.GOLD);
                        String localityID = args[3];
                        return district.removeLocality(localityID);
                    }
                    case "rail_section" -> {
                        if (args.length == 3) return Component.text("You must specify which rail section to remove: rail_section <id>", NamedTextColor.GOLD);
                        String name = args[3];
                        return district.removeSection(name);
                    }
                    default -> {
                        return Component.text("Invalid argument. Syntax: remove [locality | rail_section]", NamedTextColor.RED);
                    }
                }
            }
            case "help" -> {
                return Component.text(command.getDescription());
            }
            default -> {
                return Component.text("Invalid operator! Must be [info | set | add | remove]", NamedTextColor.RED);
            }
        }
    }
}
