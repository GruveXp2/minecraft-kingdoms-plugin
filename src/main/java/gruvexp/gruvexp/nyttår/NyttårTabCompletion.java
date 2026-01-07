package gruvexp.gruvexp.nyttår;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NyttårTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        Player p = (Player) sender;

        if (args.length == 1) {
            return List.of("reset_signpost", "spawn_outline", "animate_popout", "run_glass", "fix_year", "report", "lag_tekst", "register_center", "register_number_center", "run_snowflake", "spawn_circle", "toggle_set_blocks", "rotate_snowflake", "teleportus_vekkus", "animate_number", "delete_circle", "save_positions", "load_positions", "test");
        }
        String oper = args[0];
        switch (oper) {
            case "register_center", "spawn_circle" -> {
                Coord coord = Utils.getTargetBlock(p, 10);
                switch (args.length) {
                    case 2 -> {
                        return List.of(coord.x() + " " + coord.y() + " " +coord.z());
                    }
                    case 3 -> {
                        return List.of(coord.y() + " " +coord.z());
                    }
                    case 4 -> {
                        return List.of(String.valueOf(coord.z()));
                    }
                }
            }
            case "register_number_center", "toggle_set_blocks" -> {
                return List.of("1", "2");
            }
        }
        return List.of();
    }
}
