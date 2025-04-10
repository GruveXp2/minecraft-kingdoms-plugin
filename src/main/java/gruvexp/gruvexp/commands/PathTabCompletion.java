package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.path.Path;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PathTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return List.of();}
        Locality locality = KingdomsManager.getSelectedLocality(p);
        if (locality == null) return List.of(ChatColor.RED + "You must select a locality to work with!", "run /kingdoms select <kingdom> <district> <locality>");

        if (args.length == 1) return locality.getPathIDs().stream().map(Object::toString).toList();

        if (args.length == 2) {
            return List.of("info", "set", "remove");
        }
        String oper = args[1];
        switch (oper) {
            case "set" -> {
                if (args.length == 3) return List.of("start_pos", "turns", "branch");

                String property = args[2];
                switch (property) {
                    case "start_pos" -> {
                        return List.of(Utils.getTargetBlock(p, 10).toString());
                    }
                    case "turns" -> {
                        if (args.length == 4) return Path.DIRECTIONS.stream().toList();

                        String arg = args[args.length - 1];
                        if (Objects.equals(arg, "")) {
                            return List.of("<index: number>");
                        } else if (!arg.contains(":")) {
                            return List.of(arg + ":");
                        } else if (arg.charAt(arg.length() - 1) == ':') {
                            return Path.DIRECTIONS.stream().toList();
                        }
                    }
                    case "branch" -> {
                        if (args.length == 4) return List.of("<index: number>");
                        if (args.length == 5) {
                            List<String> out = new ArrayList<>(locality.getPathIDs().stream().toList());
                            out.add("enter_rail");
                            return out;
                        }
                        if (args.length == 6) return List.of("<enter index: number>");
                        List<String> out = locality.getHouseIDs().stream().map(Object::toString).collect(Collectors.toList());
                        out.add("station");
                        return out;
                    }
                }
            }
            case "remove" -> {
                if (args.length == 3) return List.of("branch");
                if (args.length == 4) return List.of("<index: number>");
            }
        }
        return List.of();
    }
}
