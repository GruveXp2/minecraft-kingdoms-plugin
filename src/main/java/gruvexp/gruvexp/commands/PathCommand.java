package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PathCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return true;
        }
        String usage =  ChatColor.WHITE + "\nUsage: /path <kingdom> <district> <address> [add | get | list | set | remove]";
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
            if (oper.equals("list")) {
                p.sendMessage("WIP: print list of all paths here");
                return true;
            }
            if (args.length == 4) {
                throw new IllegalArgumentException(ChatColor.RED + "You must specify path id");
            }
            String pathID = args[4];
            switch (oper) {
                case "add" -> {
                    if (args.length < 9) {
                        throw new IllegalArgumentException(ChatColor.RED + "Not enough args!\n" + ChatColor.WHITE + "Usage: add <ID> <startpos> <start direction> <turn data...>");
                    }
                    Coord startPos = new Coord(args[5], args[6], args[7]);
                    HashMap<Integer, Character> turnMap = new HashMap<>();
                    turnMap.put(0, args[8].toCharArray()[0]);
                    if (args.length > 9) {
                        List<String> turnData = Arrays.asList(args).subList(9, args.length);
                        for (String turn : turnData) {
                            String[] turnParts = turn.split(":");
                            //p.sendMessage(ChatColor.GRAY + "[DEBUG]: turnParts[] = {" + turnParts[0] + "," + turnParts[1] + "}");
                            try {
                                turnMap.put(Integer.parseInt(turnParts[0]), Path.dirToChar(turnParts[1]));
                            }
                            catch (NumberFormatException e) {
                                throw new IllegalArgumentException(String.format("%sIndex must be a number!\n%sExpected number, got %s%s%s instead", ChatColor.RED, ChatColor.WHITE, ChatColor.RED, turnParts[0], ChatColor.WHITE));
                            }
                        }
                    }
                    address.addPath(pathID, new Path(pathID, startPos, turnMap));
                    p.sendMessage("Successfully added path " + ChatColor.YELLOW + pathID);
                }
                case "get" -> {
                    Path path = address.getPath(pathID);
                    p.sendMessage(String.format("%sPath %s%s%s: %s%s%s: %s%s%s: %s%s%s:",
                            ChatColor.UNDERLINE, ChatColor.GOLD, kingdomID, ChatColor.WHITE, ChatColor.GOLD, districtID, ChatColor.WHITE, ChatColor.GOLD, addressID, ChatColor.WHITE, ChatColor.YELLOW, pathID, ChatColor.WHITE));

                    p.sendMessage("Starts at: " + ChatColor.AQUA + path.getStartPos());

                    StringBuilder turns = new StringBuilder("\nTurns: " + ChatColor.GREEN);
                    HashMap<Integer, Character> turnMap = path.getTurns();
                    for (Map.Entry<Integer, Character> turn: turnMap.entrySet()) {
                        turns.append(ChatColor.RED).append(turn.getKey()).append(ChatColor.WHITE).append(":")
                                .append(ChatColor.GREEN).append(Path.dirToStr(turn.getValue())).append(ChatColor.WHITE).append(", ");
                    }
                    p.sendMessage(turns.toString());

                    StringBuilder branches = new StringBuilder("\nBranches: " + ChatColor.GREEN);
                    HashMap<Integer, String> branchPathMap = path.getBranchPathIDs();
                    for (Map.Entry<Integer, String> branchPath: branchPathMap.entrySet()) {
                        int index = branchPath.getKey();
                        branches.append(ChatColor.RED).append(index).append(ChatColor.WHITE).append(": ")
                                .append(ChatColor.YELLOW).append(branchPath.getValue()).append(ChatColor.WHITE).append(":")
                                .append(ChatColor.RED).append(path.getBranchEnterIndex(index)).append(ChatColor.WHITE)
                                .append(", ").append(ChatColor.GOLD);
                        for (String branchAddress : path.getBranchAddresses(index)) {
                            branches.append(branchAddress).append(", ");
                        }
                        branches.delete(branches.length() - 2, branches.length());
                    }
                    p.sendMessage(branches.toString());
                }
                case "set" -> {
                    Path path = address.getPath(pathID);
                    if (args.length == 5) {
                        throw new IllegalArgumentException(ChatColor.RED + "You must choose what property to set\nUsage:" + ChatColor.WHITE + "set [turns | branch]");
                    }
                    String property = args[5];
                    switch (property) {
                        case "start_pos" -> {
                            if (args.length < 9) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args!");
                            }
                            Coord startPos = new Coord(args[6], args[7], args[8]);
                            path.setStartPos(startPos);
                            p.sendMessage("Successfully updated path turns");
                        }
                        case "turns" -> {
                            if (args.length == 6) {
                                throw new IllegalArgumentException(ChatColor.RED + "Not enough args!");
                            }
                            HashMap<Integer, Character> turnMap = new HashMap<>();
                            turnMap.put(0, args[6].toCharArray()[0]);
                            if (args.length > 7) {
                                List<String> turnData = Arrays.asList(args).subList(7, args.length);
                                for (String turn : turnData) {
                                    String[] turnParts = turn.split(":");
                                    try {
                                        turnMap.put(Integer.parseInt(turnParts[0]), Path.dirToChar(turnParts[1]));
                                    } catch (NumberFormatException e) {
                                        throw new IllegalArgumentException(String.format("%sIndex must be a number!\n%sExpected number, got %s%s%s instead", ChatColor.RED, ChatColor.WHITE, ChatColor.RED, turnParts[0], ChatColor.WHITE));
                                    }
                                }
                            }
                            path.setTurns(turnMap);
                            p.sendMessage("Successfully updated path turns");
                        }
                        case "branch" -> {
                            if (args.length < 9) {
                                throw new IllegalArgumentException(ChatColor.RED + "Too few arguments!");
                            }
                            int index;
                            try {
                                index = Integer.parseInt(args[6]);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(String.format("%sIndex must be a number!\n%sExpected number, got %s%s%s instead", ChatColor.RED, ChatColor.WHITE, ChatColor.RED, args[6], ChatColor.WHITE));
                            }
                            String targetPathID = args[7];
                            int enterIndex;
                            try {
                                enterIndex = Integer.parseInt(args[8]);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException(String.format("%sIndex must be a number!\n%sExpected number, got %s%s%s instead", ChatColor.RED, ChatColor.WHITE, ChatColor.RED, args[6], ChatColor.WHITE));
                            }
                            HashSet<String> addresses = new HashSet<>(Arrays.asList(args).subList(9, args.length));
                            path.addBranch(index, targetPathID, enterIndex, addresses);
                            p.sendMessage("Successfully added the branch at index " + ChatColor.RED + index);
                        }
                        default ->
                                throw new IllegalArgumentException(ChatColor.RED + "\n" + property + "\n is not a valid argument!");
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
