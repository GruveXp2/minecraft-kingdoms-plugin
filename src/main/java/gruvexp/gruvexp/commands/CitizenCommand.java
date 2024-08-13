package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.Citizen;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CitizenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }
        String usage =  ChatColor.WHITE + "\nUsage: /citizen <kingdom> [add | get | list | set | remove]";
        Player p = (Player) sender;

        if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Error: Too few arguments." + usage);
            return true;
        }
        try {
            String kingdomID = args[0];
            Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
            String oper = args[1];
            if (oper.equals("list")) {
                p.sendMessage("WIP: print list of all citizens here");
                return true;
            }
            if (args.length == 2) {
                throw new IllegalArgumentException(ChatColor.RED + "You must specify villager name");
            }
            String citizenName = args[2].toLowerCase();
            switch (oper) {
                case "add" -> {
                    if (args.length < 8) {
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough args!\n" + ChatColor.WHITE + "Usage: add <name> <variant> <profession> <distr> <addr> <house id>");
                    }
                    Villager.Type type = Villager.Type.valueOf(args[3].toUpperCase());
                    Villager.Profession profession = Villager.Profession.valueOf(args[4].toUpperCase());
                    String homeAddress = args[5] + " " + args[6] + " " + args[7];
                    Citizen citizen = new Citizen(citizenName, type, profession, kingdom, homeAddress);
                    kingdom.addCitizen(citizenName, citizen);
                    p.sendMessage("Successfully created new citizen " + ChatColor.GREEN + Utils.ToName(citizenName));
                }
                case "get" -> {
                    Citizen citizen = kingdom.getCitizen(citizenName);
                    if (args.length == 4 && args[3].equals("bio")) {
                        p.sendMessage(String.format("%sBio of citizen %s%s%s from %s%s%s:",
                                ChatColor.UNDERLINE, ChatColor.GREEN, Utils.ToName(citizenName), ChatColor.WHITE, ChatColor.GOLD, kingdomID, ChatColor.WHITE));
                        p.sendMessage(ChatColor.ITALIC + citizen.getBio());
                        return true;
                    }
                    p.sendMessage(String.format("%sCitizen %s%s%s in %s%s%s:",
                            ChatColor.UNDERLINE, ChatColor.GREEN, Utils.ToName(citizenName), ChatColor.WHITE, ChatColor.GOLD, kingdomID, ChatColor.WHITE));

                    p.sendMessage("Biome variant: " + ChatColor.GREEN + citizen.getType().toLowerCase());
                    p.sendMessage("Profession: " + ChatColor.GREEN + citizen.getProfession().toLowerCase());
                    String[] homeAddressParts = citizen.getHomeAddress().split(" ");
                    p.sendMessage(String.format("Home: %s%s %s %s%s", ChatColor.GOLD, homeAddressParts[0], homeAddressParts[1], ChatColor.RED, homeAddressParts[2]));
                    String workAddress = citizen.getWorkAddress();
                    if (workAddress != null) {
                        String[] workAddressParts = workAddress.split(" ");
                        p.sendMessage(String.format("Work address: %s%s %s %s", ChatColor.GOLD, workAddressParts[0], workAddressParts[1], workAddressParts[2]));
                    }
                }
                case "set" -> {
                    Citizen citizen = kingdom.getCitizen(citizenName);
                    if (args.length == 3) {
                        throw new IllegalArgumentException(ChatColor.RED + "You must choose what property to set\nUsage:" + ChatColor.WHITE + "set [home_address | work_address]");
                    }
                    String property = args[3];
                    switch (property) {
                        case "home_address" -> {
                            if (args.length < 7) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args!");
                            }
                            String homeAddress = args[4] + " " + args[5] + " " + args[6];
                            citizen.setHomeAddress(homeAddress);
                            p.sendMessage(String.format("Set home address of citizen %s%s%s to\n%s%s %s %s%s", ChatColor.GREEN, Utils.ToName(citizenName), ChatColor.WHITE, ChatColor.GOLD, args[4], args[5], ChatColor.RED, args[6]));
                        }
                        case "work_address" -> {
                            if (args.length < 7) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args!");
                            }
                            String workAddress = args[4] + " " + args[5] + " " + args[6];
                            citizen.setWorkAddress(workAddress);
                            p.sendMessage(String.format("Set work address of citizen %s%s%s to\n%s%s %s %s", ChatColor.GREEN, Utils.ToName(citizenName), ChatColor.WHITE, ChatColor.GOLD, args[4], args[5], args[6]));
                        }
                        case "bio" -> {
                            if (args.length == 4) {
                                throw new IllegalArgumentException(ChatColor.RED + "You must write a bio");
                            }
                            List<String> bioList = Arrays.asList(args).subList(4, args.length);
                            StringBuilder bio = new StringBuilder();
                            for (String word : bioList) {
                                bio.append(word).append(" ");
                            }
                            bio.delete(bio.length() - 1, bio.length());
                            citizen.setBio(bio.toString());
                        }
                        default ->
                                throw new IllegalArgumentException(ChatColor.RED + "\n" + property + "\n is not a valid argument!");
                    }
                }
                case "remove" -> throw new IllegalArgumentException(ChatColor.RED + "This functionality is not added yet!");
                case "tp" -> p.teleport(kingdom.getCitizen(citizenName).getVillager());
            }
        } catch (IllegalArgumentException e) {
            p.sendMessage(e.getMessage());
        }
        return true;
    }
}
