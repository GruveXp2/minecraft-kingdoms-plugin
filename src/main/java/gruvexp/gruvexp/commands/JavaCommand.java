package gruvexp.gruvexp.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class JavaCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        Player p = (Player) sender;
        String oper = args[0];
        if (oper.equals("tp")) {
            Location pLoc = p.getLocation();
            double Δx = Integer.parseInt(args[1]) + 0.5 - pLoc.getX();
            double Δy = Integer.parseInt(args[2])       - pLoc.getY();
            double Δz = Integer.parseInt(args[3]) + 0.5 - pLoc.getZ();
            pLoc.add(Δx, Δy, Δz);
            p.teleport(pLoc);
        }
        return true;
    }
}
