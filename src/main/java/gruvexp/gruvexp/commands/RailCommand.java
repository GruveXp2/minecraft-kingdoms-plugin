package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.data.Rail;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RailCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);
        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        // Rail command by GruveXp aka David
        // This is a command that lets players in my Minecraft server interact with the rail network, and makes it easier to create roads similar to real life.
        // when a player comes to a rail intersection, arrows will appear in the inventory and the player can choose which rail to switch to.
        // the rail network are divided into sections wich can have different properties such as speed or exit points.
        // Usage example: /rail get my_section route right.
        // usage explanation: Command returns the rail cords and direction when you hold right arrow in hand and are in section "my section"
        // if a section is not complete, minecarts will stop when they enter the sector and display error message in chat so a player can use this command to fix.

        Kingdom kingdom = KingdomsManager.getSelectedKingdom(p);
        District district = KingdomsManager.getSelectedDistrict(p);
        if (district == null) return Component.text("You must specify the scope of this command (what district you wanna work with)" +
                "\nrun /kingdoms select <kingdom> <district>", NamedTextColor.GOLD);

        String sectionID = args[0];
        Section section = district.getSection(sectionID);
        if (section == null) return Component.text(kingdom.id + ":" + district.id + " has no section named \"" + sectionID + "\"!", NamedTextColor.RED);
        if (args.length == 1) return Component.text("You must specify an operation [info | set | add | remove]", NamedTextColor.GOLD);

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                Component borderInfo = section.hasBorder() ? Component.text("Border: ").append(section.getBorder().address()).appendNewline() : Component.empty();
                Component exitValue = section.getExit() != null ? section.getExit().name() : Component.text("not set", NamedTextColor.YELLOW);
                Component lengthValue = section.getLength() > 0 ? Component.text(section.getLength() + "m", NamedTextColor.AQUA) : Component.text("not calculated", NamedTextColor.YELLOW);
                return Component.newline()
                        .append(Component.text("Rail section ", Section.LABEL_COLOR)).append(section.name()).append(Component.text(" has the following data:\n"))
                        .append(Component.text("Entry: ")).append(section.getEntry().name()).appendNewline()
                        .append(Component.text("Exit  : ")).append(exitValue).appendNewline()
                        .append(Component.text("Length: ")).append(lengthValue).appendNewline()
                        .append(Component.text("Speed: ")).append(section.speed()).appendNewline()
                        .append(borderInfo)
                        .append(section.routes());

            }
            case "set" -> {
                if (args.length == 2) return Component.text("You must specify what to set: [entry | exit | speed | route | multi_route | border] <value(s)>", NamedTextColor.GOLD);
                String property = args[2];
                switch (property) {
                    case "entry" -> {
                        if (args.length < 6) return Component.text("You must specify the coordinates: set entry <x y z>", NamedTextColor.GOLD);
                        return section.setEntry(new Coord(args[3], args[4], args[5]));
                    }
                    case "exit" -> {
                        if (args.length < 6) return Component.text("You must specify the coordinates: set exit <x y z>", NamedTextColor.GOLD);
                        return section.setExit(new Coord(args[3], args[4], args[5]));
                    }
                    case "next_section" -> { // tabcompletion som bare viser sections som har samme entry som @s exit! Hvis det ikke er noen sections, står det "found no sections to link with". Det står alltid "terminal"
                        // <rail section> set next_section <section>
                        if (args.length == 3) return Component.text("You must specify the section to link with (or if its the last stop (end)): set next_section [<section> | end]", NamedTextColor.GOLD);
                        String nextSectionID = args[3];
                        if (nextSectionID.contains("end") || Objects.equals(nextSectionID, "stop") || Objects.equals(nextSectionID, "exit")) {
                            return section.setNextSection(null);
                        } else {
                            if (section.hasBorder()) district = section.getBorder();
                            Section nextSection = district.getSection(nextSectionID);
                            if (nextSection == null) {
                                String searchDistrict = section.hasBorder() ? "the border district" : "this district";
                                return Component.text("Section \"" + nextSectionID + "\" not found in " + searchDistrict + ". If the section actually exists, you have to set a border for this section, so the server knows which district to look for the specified section", NamedTextColor.YELLOW);
                            }
                            return section.setNextSection(nextSection);
                        }
                    }
                    case "route" -> { // adder data til den spesifiserte retninga
                        // /rail <rail section> set route forward east_west section adr adr adr
                        if (args.length < 7) return Component.text("Not enough args specified! Usage: set route [forward | right | left] <rail shape> <target section> <list of adresses>", NamedTextColor.GOLD);

                        String direction = args[3]; // right
                        if (!KingdomsManager.ROUTES.contains(direction)) return Component.text("\"" + direction + "\" is not a valid direction!", NamedTextColor.RED);

                        String railShapeStr = args[4];
                        if (!KingdomsManager.RAIL_SHAPES.contains(railShapeStr)) return Component.text("\"" + railShapeStr + "\" is not a valid rail shape!", NamedTextColor.RED);
                        Rail.Shape railShape = Rail.Shape.valueOf(railShapeStr.toUpperCase()); // north_west

                        String targetSectionID = args[5];
                        if (section.hasBorder()) district = section.getBorder();
                        Section targetSection = district.getSection(targetSectionID);
                        if (targetSection == null) {
                            String searchDistrict = section.hasBorder() ? "the border district" : "this district";
                            return Component.text("Section \"" + targetSectionID + "\" not found in " + searchDistrict + ". If the section actually exists, you have to set a border for this section, so the server knows which district to look for the specified section", NamedTextColor.YELLOW);
                        }

                        HashSet<String> addresses = new HashSet<>(args.length - 6);
                        addresses.addAll(Arrays.asList(args).subList(6, args.length));

                        return section.setRoute(direction, railShape, targetSection, addresses);
                    }
                    case "border" -> { // /rail <section> set border <kdom> <distr>, og tabcompletionen foreslår border | border <currentkingdom>
                        if (args.length == 3) return Component.text("You must specify what border district to set: border <kingdom> <district>", NamedTextColor.GOLD);
                        String targetKingdomID = args[3];
                        Kingdom targetKingdom = KingdomsManager.getKingdom(targetKingdomID);
                        if (targetKingdom == null) return Component.text("Kingdom \"" + targetKingdomID + "\" doesnt exist!", NamedTextColor.RED);

                        String targetDistrictID = args[4];
                        District targetDistrict = targetKingdom.getDistrict(targetDistrictID);
                        if (targetDistrict == null) return Component.text("District \"" + targetDistrictID + "\" doesnt exist!", NamedTextColor.RED);

                        return section.setBorder(targetDistrict);
                    }
                    case "speed" -> { // /rail set <section> speed <speed>
                        if (args.length == 3) return Component.text("You must specify what speed to set: speed [normal | fast | express]", NamedTextColor.GOLD);

                        String speedName = args[3];
                        int speed = switch (speedName) {
                            case "40", "40km/h" -> 1;
                            case "70", "70km/h" -> 2;
                            case "110", "110km/h" -> 3;
                            case "140", "140km/h" -> 4;
                            default -> -1;
                        };
                        if (speed == -1) return Component.text("Speed \"" + speedName + "\" is invalid, must be 40, 70, 110, or 140 km/h", NamedTextColor.RED);
                        return section.setSpeed(speed);
                    }
                    default -> {
                        return Component.text("\"" + property + "\" is not a property of rail section!", NamedTextColor.RED);
                    }
                }
            }
            case "remove" -> { // rail remove <section> route forward
                if (args.length == 2) return Component.text("You must specify what to remove [route | border]", NamedTextColor.GOLD);

                String property = args[2];
                switch (property) {
                    case "route" -> {
                        if (args.length == 3) return Component.text("You must specify what route to remove: route [<route direction> | all]", NamedTextColor.GOLD);

                        String routeDirection = args[3];
                        if (routeDirection.equals("all")) return section.removeAllRoutes();

                        if (!KingdomsManager.ROUTES.contains(routeDirection)) return Component.text("\"" + routeDirection + "\" is not a valid direction!", NamedTextColor.RED);
                        return section.removeRoute(routeDirection);
                    }
                    case "border" -> {
                        return section.removeBorder();
                    }
                    default -> {
                        return Component.text("\"" + property + "\" is not a removable property of rail section!", NamedTextColor.RED);
                    }
                }
            }
            case "calculate_length" -> { // /rail <section> calculate_length n
                if (args.length == 2) return Component.text("You must specify which direction one drives on the rail, from the entry point: [n | s | e | w]", NamedTextColor.GOLD);
                String direction = args[2];
                if (!KingdomsManager.DIRECTIONS.contains(direction)) return Component.text("\"" + direction + "\" is not a valid direction!", NamedTextColor.RED);

                return section.calculateLength(direction, p);
            }
            default -> {
                return Component.text("Invalid operation! Must be [info | set | remove | tp | help]", NamedTextColor.RED);
            }
        }
    }
}
