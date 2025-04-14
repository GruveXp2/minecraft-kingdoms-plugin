package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class LocalityCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);
        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        District district = KingdomsManager.getSelectedDistrict(p);
        if (district == null) return Component.text("You must specify the scope of this command (what district you wanna work with)" +
                    "\nrun /kingdoms select <kingdom> <district>", NamedTextColor.GOLD);

        String localityID = args[0];
        Locality locality = district.getLocality(localityID);
        if (locality == null) return district.address().append(Component.text(" has no locality named \"" + localityID + "\"!", NamedTextColor.RED));
        if (args.length == 1) return Component.text("You must specify an operation [info | set | add | remove]", NamedTextColor.GOLD);

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                return Component.newline()
                        .append(Component.text("Locality ", Locality.LABEL_COLOR)).append(locality.name())
                        .append(Component.text(" in ")).append(district.address()).append(Component.text(":\n"))
                        .append(Component.text("Icon: ")).append(Component.text(locality.getIcon().toString().toLowerCase(), NamedTextColor.GREEN)).appendNewline()
                        .append(Component.text(locality.getHouseIDs().size())).append(Component.text(" houses: "))
                        .append(Component.text(locality.getHouseIDs().stream()
                                .map(String::valueOf).collect(Collectors.joining(", ")))).appendNewline()
                        .append(Component.text(locality.getPathIDs().size(), Path.VALUE_COLOR)).append(Component.text(" path sections", Path.LABEL_COLOR));
            }
            case "set" -> {
                if (args.length == 2) return Component.text("You must specify what to set: [icon | entrypoint] <value(s)>", NamedTextColor.GOLD);
                String property = args[2];
                switch (property) {
                    case "icon" -> {
                        if (args.length == 3) return Component.text("You must specify what item to set to: set icon <item>", NamedTextColor.GOLD);
                        String itemID = args[3];
                        Material icon = Material.getMaterial(itemID.toUpperCase());
                        if (icon == null)
                            return Component.text("Item called \"" + itemID + "\" doesnt exist", NamedTextColor.RED);
                        return locality.setIcon(icon);
                    }
                    case "entrypoint" -> {
                        if (args.length < 5) return Component.text("You must specify what section the rail station enters, and what direction the railcart will go when entering:" +
                                "\n set entrypoint <rail section> <direction>", NamedTextColor.GOLD);
                        String sectionID = args[3];
                        Section section = district.getSection(sectionID);
                        String directionStr = args[4];
                        if (!KingdomsManager.DIRECTIONS.contains(directionStr)) {
                            return Component.text("\"" + directionStr + "\" is an invalid direction!", NamedTextColor.RED);
                        }
                        return locality.setEntrypoint(section, directionStr.charAt(0));
                    }
                }
                return Component.text("Invalid property argument! Syntaxs: set icon <item>", NamedTextColor.RED);
            }
            case "add" -> {
                if (args.length == 2) return Component.text("You must specify what to add: [house | path_section]", NamedTextColor.GOLD);
                String feature = args[2];
                switch (feature) {
                    case "house" -> {
                        if (args.length == 3) return Component.text("You must specify the house number: add house <house number>", NamedTextColor.GOLD);
                        int houseNumber;
                        try {
                            houseNumber = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            return Component.text("House number must be a number!", NamedTextColor.RED);
                        }
                        return locality.addHouse(houseNumber);
                    }
                    case "path_section" -> {
                        if (args.length < 6) return Component.text("You must specify the start pos of the new path section: add path_section <path id> <start: pos>", NamedTextColor.GOLD);
                        String pathID = args[3];
                        Coord start;
                        try {
                            start = new Coord(args[5], args[6], args[7]);
                        } catch (IllegalArgumentException e) {
                            return Component.text("Coordinates must be only numbers!", NamedTextColor.RED);
                        }
                        return locality.addPath(pathID, start);
                    }
                    default -> {
                        return Component.text("Invalid argument. Syntakx: add [house | path_section] <properties>", NamedTextColor.RED);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 2) return Component.text("You must specify what to remove: [house | path_section]", NamedTextColor.GOLD);
                String feature = args[2];
                switch (feature) {
                    case "house" -> {
                        if (args.length == 3) return Component.text("You must specify which house to remove: house <house number>", NamedTextColor.GOLD);
                        int houseNumber;
                        try {
                            houseNumber = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            return Component.text("House number must be a number!", NamedTextColor.RED);
                        }
                        return locality.removeHouse(houseNumber);
                    }
                    case "path_section" -> {
                        if (args.length == 3) return Component.text("You must specify which path section to remove: path_section <id>", NamedTextColor.GOLD);
                        String pathID = args[3];
                        return locality.removePath(pathID);
                    }
                    default -> {
                        return Component.text("Invalid argument. Syntax: remove [house | path_section]", NamedTextColor.RED);
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
