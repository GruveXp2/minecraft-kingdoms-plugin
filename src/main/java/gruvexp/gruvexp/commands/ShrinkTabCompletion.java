package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShrinkTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        Player p = (Player) sender;

        Coord coord = Utils.getTargetBlock(p, 10);
        switch (args.length) {
            case 1, 4, 7 -> {
                return List.of(coord.x() + " " + coord.y() + " " +coord.z());
            }
            case 2, 5, 8 -> {
                return List.of(coord.y() + " " +coord.z());
            }
            case 3, 6, 9 -> {
                return List.of(String.valueOf(coord.z()));
            }
        }
        return new ArrayList<>(0);
    }
}
