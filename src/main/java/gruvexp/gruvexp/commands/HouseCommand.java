package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.Citizen;
import gruvexp.gruvexp.core.House;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;

public class HouseCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }
        String usage =  ChatColor.WHITE + "\nUsage: /house <kingdom> <district> <address> [add | get | list | set | remove]";
        Player p = (Player) sender;

        if (args.length < 4) {
            p.sendMessage(ChatColor.RED + "Error: Too few arguments." + usage);
            return true;
        }

        try {
            String kingdomID = args[0];
            String districtID = args[1];
            String addressID = args[2];
            Address address = KingdomsManager.getKingdom(kingdomID).getDistrict(districtID).getAddress(addressID);
            String oper = args[3];
            int houseNumber = 0;
            if (!oper.equals("list")) {
                if (args.length == 4) {
                    throw new IllegalArgumentException(ChatColor.RED + "You must specify house number");
                }
                try {
                    houseNumber = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(ChatColor.RED + "House numbers cant contain letters!");
                }
            }
            switch (oper) {
                case "add" -> {
                    address.addHouse(houseNumber);
                    p.sendMessage("Successfully added new house with ID " + ChatColor.BLUE + houseNumber);
                }
                case "get" -> {
                    House house = address.getHouse(houseNumber);
                    p.sendMessage(String.format("%sHouse %s%s%s: %s%s%s: %s%s %s%s%s:", ChatColor.UNDERLINE, ChatColor.GOLD, kingdomID, ChatColor.WHITE, ChatColor.GOLD, districtID, ChatColor.WHITE, ChatColor.GOLD, addressID, ChatColor.BLUE, houseNumber, ChatColor.WHITE));
                    HashSet<Citizen> villagers = house.getCitizens();
                    if (villagers != null) {
                        StringBuilder citizens = new StringBuilder("Villagers: " + ChatColor.GREEN);
                        for (Citizen citizen : villagers) {
                            citizens.append(citizen.getName()).append(", ");
                        }
                        citizens.delete(citizens.length() - 2, citizens.length());
                        p.sendMessage(citizens.toString());
                    }
                    Coord doorPos = house.getDoorPos();
                    if (doorPos != null) {
                        p.sendMessage(String.format("Door pos: %s%s", ChatColor.AQUA, doorPos));
                    }
                    Coord bedPos = house.getBedPos();
                    if (bedPos != null) {
                        p.sendMessage(String.format("Bed pos: %s%s", ChatColor.AQUA, bedPos));
                    }
                    Path exitPath = house.getExitPath();
                    if (exitPath != null) {
                        p.sendMessage(String.format("Exit path: %s%s", ChatColor.YELLOW, exitPath));
                    }
                }
                case "list" -> p.sendMessage("WIP: print list of all housenumbers here");
                case "set" -> {
                    House house = address.getHouse(houseNumber);
                    if (args.length == 5) {
                        throw new IllegalArgumentException(ChatColor.RED + "You must choose what property to set\nUsage:" + ChatColor.WHITE + "set [door_pos | bed_pos | exit_path]");
                    }
                    String property = args[5];
                    switch (property) {
                        case "door_pos" -> {
                            if (args.length < 9) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough coords specified!");
                            }
                            Coord doorPos = new Coord(args[6], args[7], args[8]);
                            house.setDoorPos(doorPos);
                            p.sendMessage(String.format("Successfully set door pos of %s%s %s%s%s to %s%s %s %s", ChatColor.GOLD, addressID, ChatColor.BLUE, houseNumber, ChatColor.WHITE, ChatColor.AQUA, args[6], args[7], args[8]));
                        }
                        case "bed_pos" -> {
                            if (args.length < 9) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough coords specified!");
                            }
                            Coord bedPos = new Coord(args[6], args[7], args[8]);
                            house.setBedPos(bedPos);
                            p.sendMessage(String.format("Successfully set bed pos of %s%s %s%s%s to %s%s %s %s", ChatColor.GOLD, addressID, ChatColor.BLUE, houseNumber, ChatColor.WHITE, ChatColor.AQUA, args[6], args[7], args[8]));
                        }
                        case "exit_path" -> {
                            if (args.length == 6) {
                                throw new IllegalArgumentException(ChatColor.RED + "You must specify the pathID");
                            }
                            String pathID = args[6];
                            Path exitPath = address.getPath(pathID);
                            house.setExitPath(exitPath);
                            p.sendMessage(String.format("Successfully set pathID of %s%s %s%s%s to %s%s", ChatColor.GOLD, addressID, ChatColor.BLUE, houseNumber, ChatColor.WHITE, ChatColor.YELLOW, pathID));
                        }
                    }
                }
                case "remove" -> throw new IllegalArgumentException(ChatColor.RED + "This functionality is not added yet!");
            }
        } catch (IllegalArgumentException e) {
            p.sendMessage(e.getMessage());
        }



        return true;
    }
}
