package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PathCommand implements CommandExecutor {
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
                "\nrun /kingdoms select <kingdom> <district> <locality>", NamedTextColor.GOLD);

        String pathID = args[0];
        Path path = locality.getPath(pathID);
        if (path == null) return Component.text(locality.id + " has no path section called \"" + pathID + "\"", NamedTextColor.RED);
        if (args.length == 1) return Component.text("You must specify an operation [info | set | remove]", NamedTextColor.GOLD);

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                return Component.newline()
                        .append(Component.text("Path ", Path.LABEL_COLOR)).append(path.name()).append(Component.text(" has the following data:\n"))
                        .append(Component.text("Starts at ")).append(path.getStartPos().name()).appendNewline()
                        .append(Component.text("Turns: ")).append(path.turns()).appendNewline()
                        .append(Component.text("Branches: ")).appendNewline().append(path.branches());

            }
            case "set" -> {
                if (args.length == 2) return Component.text("You must specify what to set: [door_pos | bed_pos | exit_path] <value>", NamedTextColor.GOLD);
                String property = args[2];
                switch (property) {
                    case "start_pos" -> {
                        if (args.length < 6) return Component.text("You must specify the coordinates: set door_pos <x y z>", NamedTextColor.GOLD);
                        return path.setStartPos(new Coord(args[3], args[4], args[5]));
                    }
                    case "turns" -> {
                        if (args.length == 3) return Component.text("You must specify the first turn, eventually other turns and where: set turns <start direction> <other turns (index:direction)> ...", NamedTextColor.GOLD);
                        HashMap<Integer, Character> turnMap = new HashMap<>();
                        turnMap.put(0, args[4].toCharArray()[0]);
                        if (args.length > 5) {
                            List<String> turnData = Arrays.asList(args).subList(5, args.length);
                            for (String turn : turnData) {
                                String[] turnParts = turn.split(":");
                                int branchingIndex;
                                try {
                                    branchingIndex = Integer.parseInt(turnParts[0]);
                                } catch (NumberFormatException e) {
                                    return Component.text("Path index must be a number! Expected <number>:<direction>, got " + turn + " instead", NamedTextColor.RED);
                                }
                                turnMap.put(branchingIndex, Path.dirToChar(turnParts[1]));
                            }
                        }
                        return path.setTurns(turnMap);
                    }
                    case "branch" -> {
                        if (args.length < 5) return Component.text("You must specify the following for your branch: set branch <index> <path section> <enter index?>", NamedTextColor.GOLD);
                        int index;
                        try {
                            index = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            return Component.text("Path index must be a number, not \"" + args[3] + "\"", NamedTextColor.RED);
                        }

                        String targetPathID = args[4];
                        Path targetPath = locality.getPath(targetPathID);
                        if (targetPath == null) return locality.address().append(Component.text(" has no path section named \"" + targetPathID + "\"!", NamedTextColor.RED));

                        int enterIndex;
                        try {
                            enterIndex = Integer.parseInt(args[5]);
                        } catch (NumberFormatException e) {
                            return Component.text("Entering index of target path must be a number, not \"" + args[5] + "\"", NamedTextColor.RED);
                        }
                        HashSet<String> addresses = new HashSet<>(Arrays.asList(args).subList(6, args.length));
                        return path.addBranch(index, targetPath, enterIndex, addresses);
                    }
                    default -> {
                        return Component.text("\"" + property + "\" is not a property of a path section", NamedTextColor.RED);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 2) return Component.text("You must specify what to remove: (branch)", NamedTextColor.GOLD);
                String feature = args[2];
                if (feature.equals("branch")) {
                    if (args.length == 3) return Component.text("You must specify which branch to remove from this path: remove branch <index>", NamedTextColor.GOLD);

                    int index;
                    try {
                        index = Integer.parseInt(args[3]);
                    } catch (NumberFormatException e) {
                        return Component.text("Path index must be a number, not \"" + args[3] + "\"", NamedTextColor.RED);
                    }

                    return path.removeBranch(index);
                }
                return Component.text("\"" + feature + "\" is not a removable feature in a house", NamedTextColor.RED);
            }
            default -> {
                return Component.text("Invalid operator! Must be [info | set | remove]", NamedTextColor.RED);
            }
        }
    }
}
