package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.HomeManager;
import gruvexp.gruvexp.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static gruvexp.gruvexp.HomeManager.homeLocations;

public class HomeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        Player p = (Player) sender;

        if (args.length == 0) {
            try {
                String coordStr = homeLocations.get(p.getPlayerListName());
                String[] coords = coordStr.split(" ");
                if (p.getName().equals("bossfight3")) {
                    p.teleport(new Location(Main.WORLD_NETHER, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2])));
                } else {
                    p.teleport(new Location(Main.WORLD, Double.parseDouble(coords[0]), Double.parseDouble(coords[1]), Double.parseDouble(coords[2])));
                }
            } catch (NullPointerException e) {
                p.sendMessage(Component.text("You dont have a registered home!", NamedTextColor.RED).appendNewline()
                        .append(Component.text("To set your home, type ", NamedTextColor.WHITE))
                        .append(Component.text("/home set", NamedTextColor.AQUA))
                        .append(Component.text(", and your location will be saved as your home", NamedTextColor.WHITE)));
            }
        }

        if (args.length == 1 && Objects.equals(args[0], "set")) {
            Location loc = p.getLocation();
            String coords = (Math.round(loc.getX() * 100.0) / 100.0)+" "+loc.getY()+" "+(Math.round(loc.getZ() * 100.0) / 100.0);
            homeLocations.put(p.getPlayerListName(), coords);
            p.sendMessage(Component.text("Your home adress is now ")
                    .append(Component.text(coords, NamedTextColor.AQUA)));
            HomeManager.saveData();
        }
        // man blir tpa til hjemmet sitt når man skriver /home. skriv /home set <pos> for å sette, og /home get for å få den nåværende posisjonen.
        return true;
    }
}
