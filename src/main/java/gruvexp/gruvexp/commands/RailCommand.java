package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.Section;
import gruvexp.gruvexp.rail.CalculateLength;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class RailCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // Rail command by GruveXp aka David
        // This is a command that lets players in my Minecraft server interact with the rail network, and makes it easier to create roads similar to real life.
        // when a player comes to a rail intersection, arrows will appear in the inventory and the player can choose which rail to switch to.
        // the rail network are divided into sections wich can have different properties such as speed or exit points.
        // Usage example: /rail get my_section route right.
        // usage explanation: Command returns the rail cords and direction when you hold right arrow in hand and are in section "my section"
        // if a section is not complete, minecarts will stop when they enter the sector and display error message in chat so a player can use this command to fix.

        if (!(sender instanceof Player)) {
            return true;
        }
        String usage =  ChatColor.WHITE + "\nUsage: /rail [add | get | modify | show | remove | calculate_length | entrypoints | addresses]";
        Player p = (Player) sender;

        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "Error: No arguments." + usage);
            return true;
        } else if (args.length < 4 && !Objects.equals(args[0], "entrypoint")&& !Objects.equals(args[0], "addresses")) {
            p.sendMessage(ChatColor.RED + "Error: No section specified!" + usage);
            return true;
        }

        String oper = args[0];
        String property;
        String sectionID = "";
        Section section;
        if (!Objects.equals(oper, "entrypoint")&& !Objects.equals(oper, "addresses")) {
            sectionID = args[3];
        }
        try {
            switch (oper) {
                case "add" -> { // add <kingdom> <district> <new section>
                    Kingdom kingdom = KingdomsManager.getKingdom(args[1]);
                    District district = kingdom.getDistrict(args[2]);
                    district.addSection(sectionID, new Section());
                    p.sendMessage("Section " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " successfully added");
                    KingdomsManager.save = true;
                }
                case "get" -> {
                    if (args.length == 4) {
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: get <kingdom> <district> <section> [entry | exit | length | route | monoroute | speed | border]");
                    }
                    Kingdom kingdom = KingdomsManager.getKingdom(args[1]);
                    District district = kingdom.getDistrict(args[2]);
                    section = district.getSection(sectionID);
                    property = args[4];
                    if (Objects.equals(property, "routes")) {
                        p.sendMessage(section.printRoutes());
                        return true;
                    }
                    p.sendMessage(section.print(property));
                }
                case "set" -> { // raildata set <section> entry x y z
                    if (args.length == 4) {
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: set <kingdom> <district> <section> [entry | exit | route | monoroute | speed | border]");
                    }
                    String kingdomID = args[1];
                    Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                    String districtID = args[2];
                    District district = kingdom.getDistrict(districtID);
                    section = district.getSection(sectionID);
                    property = args[4];
                    switch (property) {
                        case "entry" -> {
                            if (args.length < 8) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough coordinates specified!" + ChatColor.WHITE + "\nUsage: set <section> entry <pos>");
                            }
                            section.setEntry(new Coord(args[5], args[6], args[7]));
                            p.sendMessage("Set section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " entry point to " + ChatColor.AQUA + args[5] + ", " + args[6] + ", " + args[7]);
                        }
                        case "exit" -> {
                            if (args.length < 8) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough coordinates specified!" + ChatColor.WHITE + "\nUsage: set <section> exit <pos>");
                            }
                            section.setExit(new Coord(args[5], args[6], args[7]));
                            p.sendMessage("Set section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " exit point to " + ChatColor.AQUA + args[5] + ", " + args[6] + ", " + args[7]);
                        }
                        case "route" -> { // adder data til den spesifiserte retninga /rail set <3> route forward east_west section adr adr adr
                            if (args.length < 9) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: set <section> route [forward | right | left] <rail shape> <target section> <list of adresses>");
                            }
                            String direction = args[5]; // right
                            if (!KingdomsManager.ROUTES.contains(direction)) {
                                throw new IllegalArgumentException(ChatColor.RED + "\"" + direction + "\" is not a valid direction!");
                            }
                            String railShape = args[6]; // north_west
                            if (!KingdomsManager.RAIL_SHAPES.contains(railShape)) {
                                throw new IllegalArgumentException(ChatColor.RED + "\"" + railShape + "\" is not a valid rail shape!");
                            }
                            String targetSectionID = args[7];
                            HashSet<String> addresses = new HashSet<>(args.length - 8);
                            addresses.addAll(Arrays.asList(args).subList(8, args.length));

                            section.setRoute(direction, railShape, targetSectionID, addresses);

                            StringBuilder addressesStr = new StringBuilder(ChatColor.GOLD + "");
                            for (String address : addresses) {
                                addressesStr.append(address).append(", ");
                            }
                            addressesStr.delete(addressesStr.length() - 2, addressesStr.length());
                            p.sendMessage("Successfully set data for route " + ChatColor.RED + direction + ChatColor.WHITE + ":");
                            p.sendMessage(" - Enters section: " + ChatColor.LIGHT_PURPLE + targetSectionID);
                            p.sendMessage(" - Changes rail shape to " + ChatColor.GREEN + railShape);
                            p.sendMessage(" - Addresses: " + addressesStr);

                            String borderKingdomID = section.getBorderKingdom();
                            String borderDistrictID = section.getBorderDistrict();
                            if (borderKingdomID != null) { // hvis man kjører inn i et annet kingdom/distrikt, er det seksjonene der som gjelder
                                kingdom = KingdomsManager.getKingdom(borderKingdomID);
                            }
                            if (borderDistrictID != null) {
                                district = kingdom.getDistrict(borderDistrictID);
                            }
                            if (district.notContainsSection(targetSectionID)) {
                                p.sendMessage(ChatColor.YELLOW + "The section \"" + targetSectionID + "\" doesnt exist, for the route to function properly, specified section must be added or changed to another section");
                            }
                            if (section.hasMonoRoute()) {
                                p.sendMessage(ChatColor.YELLOW + "Monoroute must be removed for route data to take effect");
                            }
                        }
                        case "monoroute" -> {
                            if (args.length == 5) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: set <section> monoroute <target section>");
                            }
                            String targetSectionID = args[5];
                            if (targetSectionID.contains("end") || Objects.equals(targetSectionID, "stop") || Objects.equals(targetSectionID, "exit")) {
                                section.setMonoroute("end");
                                p.sendMessage("Successfully set " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " as an endpoint");
                            } else {
                                section.setMonoroute(targetSectionID);
                                p.sendMessage("Successfully set monoroute of " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " to " + ChatColor.LIGHT_PURPLE + targetSectionID);
                                String borderKingdomID = section.getBorderKingdom();
                                String borderDistrictID = section.getBorderDistrict();

                                if (borderKingdomID != null) { // hvis man kjører inn i et annet kingdom/distrikt, er det seksjonene der som gjelder
                                    kingdom = KingdomsManager.getKingdom(borderKingdomID);
                                }
                                if (borderDistrictID != null) {
                                    district = kingdom.getDistrict(borderDistrictID);
                                }
                                if (district.notContainsSection(targetSectionID)) {
                                    p.sendMessage(ChatColor.YELLOW + "The section \"" + targetSectionID + "\" doesnt exist, for the route to function properly, specified section must be added or changed to another section");
                                }
                            }
                            if (section.hasRoutes()) {
                                p.sendMessage(ChatColor.YELLOW + "Monoroute data will overwrite route data");
                            }
                        }
                        case "border" -> {
                            if (args.length < 6) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: set <section> border <kingdom> <district>");
                            }
                            String targetKingdomID = null;
                            String targetDistrictID;
                            if (args.length == 6) {
                                targetDistrictID = args[5];
                            } else {
                                targetKingdomID = args[5];
                                targetDistrictID = args[6];
                            }
                            Kingdom targetKingdom = KingdomsManager.getKingdom(targetKingdomID);
                            targetKingdom.getDistrict(targetDistrictID); // bruker ikke den returnerte verdien, men hvis distriktet ikke fins, throwes det exception
                            section.setBorder(targetKingdomID, targetDistrictID);
                            p.sendMessage("Set border of " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " to " + ChatColor.GOLD + targetKingdomID + " : " + targetDistrictID);
                        }
                        case "speed" -> { // /rail set <section> speed <speed>
                            if (args.length < 6) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: set <section> speed [normal | fast | express]");
                            }
                            String speed = args[5];
                            switch (speed) {
                                case "normal" -> {
                                    section.setSpeed(1);
                                    speed = ChatColor.YELLOW + speed;
                                }
                                case "fast" -> {
                                    section.setSpeed(2);
                                    speed = ChatColor.BLUE + speed;
                                }
                                case "express" -> {
                                    section.setSpeed(3);
                                    speed = ChatColor.LIGHT_PURPLE + speed;
                                }
                                default ->
                                        throw new IllegalArgumentException(ChatColor.RED + "Invalid speed!" + ChatColor.WHITE + "\nUsage: set <section> speed [normal | fast | express]");
                            }
                            p.sendMessage("Set speed of " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " to " + speed);
                        }
                        case "name" -> { // /rail set <section> name <new name>
                            if (args.length == 5) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: set <section> name <new name>");
                            }
                            if (Objects.equals(args[5], sectionID)) {
                                throw new IllegalArgumentException(ChatColor.YELLOW + "Nothing happened, section already had that name");
                            }
                            String new_name = args[5];
                            district.removeSection(sectionID);
                            district.addSection(new_name, section);
                            p.sendMessage("Section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " changed name to " + ChatColor.LIGHT_PURPLE + new_name);
                        }
                        default ->
                                throw new IllegalArgumentException(ChatColor.RED + "Invalid operation!" + ChatColor.WHITE + "\nUsage: set <section> [entry | exit | route | monoroute | speed | border]");
                    }
                    KingdomsManager.save = true;
                }
                case "remove" -> { // rail remove <section> route forward
                    if (args.length == 2) {
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough args specified!" + ChatColor.WHITE + "\nUsage: remove <kingdom> <district> <section>");
                    }
                    String kingdomID = args[1];
                    Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                    String districtID = args[2];
                    District district = kingdom.getDistrict(districtID);
                    if (args.length == 4) {
                        district.removeSection(sectionID);
                    p.sendMessage("The section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID + ChatColor.WHITE + " was removed");
                        if (!p.getPlayerListName().equals("GruveXp")) { // hvis noen bugger og sletter stuff så får jeg greie på det
                            Player gruvexp = Bukkit.getPlayer("GruveXp");
                            if (gruvexp != null) {
                                gruvexp.sendMessage(p.getPlayerListName() + " has removed section " + ChatColor.LIGHT_PURPLE + sectionID);
                            }
                        }
                        return true;
                    }
                    // hvis man skriver no mer så er det ikke seksjonen man fjerner, men en egenskap
                    section = district.getSection(sectionID);
                    property = args[4];
                    switch (property) {
                        case "route":
                            if (args.length == 5) {
                                throw new IllegalArgumentException(ChatColor.RED + "You must select what route(s) to remove!" + ChatColor.WHITE + "\nUsage: remove route <route>");
                            }
                            String route = args[5];

                            if (Objects.equals(route, "all")) {
                                if (section.hasRoutes()) {
                                    section.removeAllRoutes();
                                    p.sendMessage("Removed all routes in section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID);
                                } else {
                                    p.sendMessage(ChatColor.YELLOW + "Nothing happened, there was no routes to remove");
                                }
                                return true;
                            }
                            if (section.hasRoute(route)) {
                                section.removeRoute(route);
                                p.sendMessage("Route " + ChatColor.RED + route + ChatColor.WHITE + " successfully removed in section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID);
                            } else {
                                p.sendMessage(ChatColor.YELLOW + "Nothing happened, that route didnt exist in the first place");
                            }
                            break;
                        case "monoroute":
                            if (section.hasMonoRoute()) {
                                section.setMonoroute("");
                            } else {
                                p.sendMessage(ChatColor.YELLOW + "Nothing happened, this section doesnt have a monoroute");
                            }
                            break;
                        case "border":
                            if (section.hasBorder()) {
                                section.removeBorder();
                                p.sendMessage("Removed all borders in section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.WHITE + " : " + ChatColor.LIGHT_PURPLE + sectionID);
                            }
                            break;
                        default:
                            p.sendMessage(ChatColor.RED + "\"" + oper + "\" is not a valid operation!" + usage);
                            break;
                    }
                    KingdomsManager.save = true;
                }
                case "calculate_length" -> { // /rail calculate_distance section n
                    if (args.length == 4) {
                        throw new IllegalArgumentException(ChatColor.RED + "You need to specify the direction to search." + ChatColor.WHITE + "\nUsage: calculate_length <section> [n | s | e | w]");
                    }
                    String direction = args[4];
                    if (!KingdomsManager.DIRECTIONS.contains(direction)) {
                        throw new IllegalArgumentException(ChatColor.RED + "\"" + direction + "\" is an invalid direction!" + ChatColor.WHITE + "\nUsage: calculate_length <section> [n | s | e | w]");
                    }
                    String kingdomID = args[1];
                    Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                    String districtID = args[2];
                    District district = kingdom.getDistrict(districtID);
                    if (district.notContainsSection(sectionID)) {
                        throw new IllegalArgumentException(ChatColor.RED + "\"" + sectionID + "\" is an invalid section!" + ChatColor.WHITE + "\nUsage: calculate_length <section> [n | s | e | w]");
                    }
                    section = district.getSection(sectionID);
                    if (section.getEntry() == null || section.getExit() == null) {
                        throw new IllegalArgumentException(ChatColor.RED + "Cannot calculate length! Section must have an entry and exit point");
                    }
                    new CalculateLength(kingdomID, districtID, sectionID, direction.toCharArray()[0], p).runTaskTimer(Main.getPlugin(), 0, 1);
                    KingdomsManager.save = true;
                }
                case "entrypoint" -> {
                    if (args.length < 5) { // entrypoints add
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough arguments!" + ChatColor.WHITE + "\nUsage: entrypoint [add | remove | get] <address>");
                    }
                    String oper2 = args[1]; // add
                    String kingdomID = args[2];
                    Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                    String districtID = args[3];
                    District district = kingdom.getDistrict(districtID);
                    String address = args[4]; // central_station
                    if (!district.hasAddress(address)) {
                        throw new IllegalArgumentException(ChatColor.RED + "Address \"" + address + "\" doesnt exist!");
                    }
                    switch (oper2) {
                        case "add" -> { // entrypoint add  <kingdom> <district> <address> <section> <dir>
                            //       /rail entrypoint add   pyralix   central western_hills western_hills_n e
                            //                 0       1       2         3          4              5        6
                            if (args.length < 7) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough arguments!" + ChatColor.WHITE + "\nUsage: add <kingdom> <district> <address> <dir> <section>");
                            }
                            if (district.getEntrypoint(address) != null) {
                                throw new IllegalArgumentException(ChatColor.RED + "Entrypoint at address " + address + " already exist!");
                            }
                            sectionID = args[5];
                            if (district.notContainsSection(sectionID)) {
                                throw new IllegalArgumentException(ChatColor.RED + "Section \"" + sectionID + "\" doesnt exist!");
                            }
                            if (!KingdomsManager.DIRECTIONS.contains(args[6])) {
                                throw new IllegalArgumentException(ChatColor.RED + "\"" + args[6] + "\" is an invalid direction!");
                            }
                            char dir = args[6].toCharArray()[0];

                            district.setEntrypoint(kingdomID, districtID, address, sectionID, dir);
                            p.sendMessage("Added entrypoint at " + ChatColor.GOLD + kingdomID + " " + districtID + " " + address);
                            KingdomsManager.save = true;
                        }
                        case "get" -> { // entrypoint get  <kingdom> <district> <address>
                            Entrypoint entrypoint = district.getEntrypoint(address);
                            if (entrypoint == null) {
                                throw new IllegalArgumentException(ChatColor.RED + "Section \"" + sectionID + "\" has no entrypoint");
                            }
                            p.sendMessage("Entrypoint is located at ");
                            p.sendMessage(ChatColor.GOLD + entrypoint.getKingdomID() + " " + entrypoint.getDistrictID() + " " + entrypoint.getAddress());
                            Coord coord = entrypoint.getCoord();
                            p.sendMessage("at coords " + ChatColor.AQUA + coord.getX() + " " + coord.getY() + " " + coord.getZ());
                            p.sendMessage("and enters section " + ChatColor.LIGHT_PURPLE + entrypoint.getSectionID());
                        }
                        case "remove" -> { // entrypoint remove  <kingdom> <district> <address>
                            district.removeEntrypoint(address);
                            p.sendMessage("Removed entrypoint at " + ChatColor.GOLD + kingdomID + " " + districtID + " " + address);
                            KingdomsManager.save = true;
                        }
                    }
                }
                case "addresses" -> {
                    if (args.length == 1) { // addresses add
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough arguments!." + ChatColor.WHITE + "\nUsage: addresses [add | get] <kingdom> <...>");
                    }
                    String oper2 = args[1];
                    if (Objects.equals(oper2, "add")) {
                        if (args.length == 3) { // addresses add
                            throw new IllegalArgumentException(ChatColor.RED + "Not enough arguments!." + ChatColor.WHITE + "\nUsage: addresses add <kingdom> <...> <material>");
                        }
                        String kingdomID = args[2];
                        if (args.length == 4) {
                            KingdomsManager.addKingdom(kingdomID, args[3]);
                            p.sendMessage("Successfully added new kingdom " + ChatColor.GOLD + kingdomID + ChatColor.WHITE + " with " + args[3] + " as king");
                            return true;
                        }
                        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                        String districtID = args[3];
                        Material material;
                        if (args.length == 5) {
                            material = Utils.getMaterial(args[4]);
                            kingdom.addDistrict(districtID, new District(material));
                            p.sendMessage("Successfully added new district " + ChatColor.GOLD + districtID + ChatColor.WHITE + " to " + ChatColor.GOLD + kingdomID);
                            return true;
                        }
                        String address = args[4];
                        material = Utils.getMaterial(args[5]);
                        p.sendMessage("Successfully added new address " + ChatColor.GOLD + address + ChatColor.WHITE + " to " + ChatColor.GOLD + districtID + ChatColor.WHITE + " in " + ChatColor.GOLD + kingdomID);
                        kingdom.getDistrict(districtID).addAddress(address, material);
                        KingdomsManager.save = true;
                    } else if (Objects.equals(oper2, "get")) {
                        if (args.length == 2) {
                            Set<String> kingdoms = KingdomsManager.getKingdomIDs();
                            StringBuilder message = new StringBuilder("There are " + kingdoms.size() + " kingdoms:\n");
                            for (String kingdom : kingdoms) {
                                message.append(kingdom).append(", ");
                            }
                            p.sendMessage(message.toString());
                        }
                        String kingdomID = args[3];
                        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                        if (args.length == 4) {
                            Set<String> districts = kingdom.getDistrictIDs();
                            StringBuilder message = new StringBuilder("There are " + districts.size() + " districts in " + kingdomID + ":\n");
                            for (String district : districts) {
                                message.append(district).append(", ");
                            }
                            p.sendMessage(message.toString());
                        }
                        String districtID = args[4];
                        District district = kingdom.getDistrict(districtID);
                        if (args.length == 5) {
                            Set<String> addresses = district.getAddressIDs();
                            StringBuilder message = new StringBuilder("There are " + addresses.size() + " addresses in " + districtID + ":\n");
                            for (String address : addresses) {
                                message.append(address).append(", ");
                            }
                            message.delete(message.length() - 2, message.length());
                            p.sendMessage(message.toString());
                        }

                    } else {
                        throw new IllegalArgumentException(ChatColor.RED + "\"" + oper2 + "\" is not a valid operation!" + ChatColor.WHITE + "\nUsage: addresses [add | get] <kingdom> <...>");
                    }
                }
                default -> p.sendMessage(ChatColor.RED + "\"" + oper + "\" is not a valid operation!" + usage);
            }
            StringBuilder out = new StringBuilder();
            for (String arg : args) {
                out.append(" ").append(arg);
            }
            for (Player q : Bukkit.getOnlinePlayers()) {
                if (q != p) {
                    q.sendMessage(ChatColor.GRAY + "[" + p.getPlayerListName() + ChatColor.GRAY + " performed command /rail" + out + "]");
                }
            }
        } catch (IllegalArgumentException e) {
            p.sendMessage(e.getMessage());
        }
        return true;
    }
}
