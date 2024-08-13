package gruvexp.gruvexp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TestTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 1) {
            return List.of("rail", "round", "cart", "wp", "1", "2", "3", "4");
        }
        String oper = args[0];
        switch (oper) {
            case "cart" -> {
                if (args.length > 2) {return List.of("");}
                return List.of("teleportCart", "teleportCart2", "mountCart", "unMountCart", "rotate1", "rotate2", "rotate3", "rotate4", "packetMount1", "packetMount2", "packetRotate");
            }
            default -> {
                return List.of("No tabcompletions added yet");
            }
        }
    }
}
