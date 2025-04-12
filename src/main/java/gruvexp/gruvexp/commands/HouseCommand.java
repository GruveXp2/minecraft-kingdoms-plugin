package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.*;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class HouseCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);
        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        Locality locality = KingdomsManager.getSelectedLocality(p);
        if (locality == null) return Component.text("You must specify the scope of this command (what locality you wanna work with)" +
                "\nrun /kingdoms select <kingdom> <district> <locality>", NamedTextColor.RED);

        int houseNumber;
        try {
            houseNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return Component.text("House number must be a number!", NamedTextColor.RED);
        }
        House house = locality.getHouse(houseNumber);
        if (house == null) return Component.text(locality.id + " has no house with number " + houseNumber + "!", NamedTextColor.RED);
        if (args.length == 1) return Component.text("You must specify an operation [info | set | add | remove]");

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                Component doorLocation = house.getDoorPos() != null ? house.getDoorPos().name() : Component.text("none", NamedTextColor.YELLOW);
                Component bedLocation = house.getBedPos() != null ? house.getBedPos().name() : Component.text("none", NamedTextColor.YELLOW);
                Component residents = !house.getResidents().isEmpty() ?
                        Component.text(String.join(", ", house.getResidents().stream().map(c -> c.name).collect(Collectors.toSet())), NamedTextColor.GREEN) :
                        Component.text("none", NamedTextColor.YELLOW);

                Component pathSection = house.getExitPath() != null ? Component.text("Connected to path section ").append(house.getExitPath().name()) :
                        Component.text("Not connected to any path section", NamedTextColor.YELLOW);

                return Component.text("House ").append(house.name()).append(Component.text(" has the following data:")).appendNewline()
                        .append(Component.text("Door location: ")).append(doorLocation).appendNewline()
                        .append(Component.text("Bed location: ")).append(bedLocation).appendNewline()
                        .append(Component.text("Residents: ").append(residents)).appendNewline()
                        .append(pathSection);
            }
            case "set" -> {
                if (args.length == 2) return Component.text("You must specify what to set: [door_pos | bed_pos | exit_path] <value>", NamedTextColor.RED);
                String property = args[2];
                switch (property) {
                    case "door_pos" -> {
                        if (args.length < 6) return Component.text("You must specify the coordinates: set door_pos <x y z>", NamedTextColor.RED);
                        return house.setDoorPos(new Coord(args[3], args[4], args[5]));
                    }
                    case "bed_pos" -> {
                        if (args.length < 6) return Component.text("You must specify the coordinates: set bed_pos <x y z>", NamedTextColor.RED);
                        return house.setBedPos(new Coord(args[3], args[4], args[5]));
                    }
                    case "exit_path" -> {
                        if (args.length == 3) return Component.text("You must specify what path section to set: path <path section>", NamedTextColor.RED);
                        String pathID = args[3];
                        Path path = locality.getPath(pathID);
                        if (path == null) return locality.address().append(Component.text(" has no path section named \"" + pathID + "\"!", NamedTextColor.RED));
                        return house.setExitPath(path);
                    }
                    default -> {
                        return Component.text("\"" + property + "\" is not a property of a house", NamedTextColor.RED);
                    }
                }
            }
            case "add" -> {
                if (args.length == 2) return Component.text("You must specify what to add: (resident)", NamedTextColor.RED);
                String feature = args[2];
                if (feature.equals("resident")) {
                    if (args.length == 3)
                        return Component.text("You must specify which resident to add to this house: add resident <citizen>");
                    String citizenName = args[3];
                    Kingdom kingdom = locality.getDistrict().getKingdom();
                    Citizen citizen = kingdom.getCitizen(citizenName);
                    if (citizen == null) return Component.text("Found no citizen with name \"" + citizenName + "\" in kingdom ", NamedTextColor.RED).append(kingdom.name());
                    citizen.setHome(house);
                    return house.addResident(citizen);
                }
                return Component.text("\"" + feature + "\" is not a feature that can be added to a house", NamedTextColor.RED);
            }
            case "remove" -> {
                if (args.length == 2) return Component.text("You must specify what to remove: (resident)", NamedTextColor.RED);
                String feature = args[2];
                if (feature.equals("resident")) {
                    if (args.length == 3) return Component.text("You must specify which resident to remove from this house: remove resident <citizen>");
                    String citizenName = args[3];
                    Kingdom kingdom = locality.getDistrict().getKingdom();
                    Citizen citizen = kingdom.getCitizen(citizenName);
                    if (citizen == null) return Component.text("Found no citizen with name \"" + citizenName + "\" in kingdom ", NamedTextColor.RED).append(kingdom.name());

                    citizen.setHome(null);
                    return house.removeResident(citizen);
                }
                return Component.text("\"" + feature + "\" is not a feature that can be removed from a house", NamedTextColor.RED);
            }
            default -> {
                return Component.text("Invalid operator! Must be [info | set | add | remove]", NamedTextColor.RED);
            }
        }
    }
}
