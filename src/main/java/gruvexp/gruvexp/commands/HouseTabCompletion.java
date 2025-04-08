package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.*;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HouseTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return List.of();}
        Locality locality = KingdomsManager.getSelectedLocality(p);
        if (locality == null) return List.of(ChatColor.RED + "You must select a locality to work with!", "run /kingdoms select <kingdom> <district> <locality>");

        if (args.length == 1) return locality.getHouseIDs().stream().map(Object::toString).toList();
        int houseNumber;
        try {
            houseNumber = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            return List.of(ChatColor.RED + "House number must be a number!");
        }
        House house = locality.getHouse(houseNumber);
        if (house == null) return List.of(ChatColor.RED + "Unknown house with house number " + args[0]);

        if (args.length == 2) return List.of("info", "set", "add resident", "remove resident");
        String oper = args[1];
        switch (oper) {
            case "set" -> {
                if (args.length == 3) return List.of("door_pos", "bed_pos", "exit_path");
                String property = args[2];
                switch (property) {
                    case "door_pos", "bed_pos" -> {
                        Coord coord = Utils.getPlayerBlockCoords(p);
                        return List.of(coord.toString());
                    }
                    case "exit_path" -> {
                        return locality.getPathIDs().stream().toList();
                    }
                }
            }
            case "add", "remove" -> {
                if (args.length == 3) return List.of("resident");
                if (args.length == 4 && args[2].equals("resident")) return locality.getDistrict().getKingdom().getCitizenNames().stream().toList();
            }
        }
        return List.of();
    }
}
