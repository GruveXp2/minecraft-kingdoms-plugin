package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.CartManager;
import gruvexp.gruvexp.rail.CircleCart;
import gruvexp.gruvexp.rail.Test;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        //sender.sendMessage(ChatColor.RED + "This command is deactivated (only used when GruveXp tests stuff)");
        //return true;

        Player p = (Player) sender;
        if (args.length == 0) {
            p.sendMessage(ChatColor.RED + "For få args. [villager | rail | round]");
            return true;
        }
        switch (args[0]) {
            case "rail" -> {
                if (args.length < 9) {
                    p.sendMessage("Skriv inn fler args. /test rail <cart uuid> <start kdom> <start distr> <start section> <dir> <kdom> <distr> <locality>");
                    return true;
                }
                CartManager.driveCart(UUID.fromString(args[1]), p,
                        KingdomsManager.getKingdom(args[2]).getDistrict(args[3]).getSection(args[4]), args[5].toCharArray()[0],
                        KingdomsManager.getKingdom(args[6]).getDistrict(args[7]).getLocality(args[8]));
            }
            case "round" -> {
                if (args.length < 4) {
                    p.sendMessage("Skriv inn fler args. /test round cartuuid radius time");
                    return true;
                }
                Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(args[1]));
                double radius = Double.parseDouble(args[2]);
                double sekunder = Double.parseDouble(args[3]);
                new CircleCart(cart, radius, sekunder);
            }
            case "wp" -> KingdomsManager.getKingdom("pyralix").getCitizen("bob_core").goToWork();
            case "1" -> p.getLocation().setPitch(40);
            case "4" -> Main.spamPackets = !Main.spamPackets;
            case "cart" -> {
                String oper = args[1];

                switch (oper) {
                    case "mountCart" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.mountCart(p, cart);
                    }
                    case "unMountCart" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.unMountCart(p, cart);
                    }
                    case "teleportCart" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        cart.teleport(new Location(Main.WORLD, -3236, 113, 2175));
                    }
                    case "teleportCart2" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.unMountCart(p, cart);
                        cart.teleport(new Location(Main.WORLD, -3236, 113, 2175));
                        Test.mountCart(p, cart);
                    }
                    case "rotate1" -> Test.rotate1(p);
                    case "rotate2" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.rotate2(p, cart);
                    }
                    case "rotate3" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.rotate3(p, cart);
                    }
                    case "rotate4" -> Test.rotate4(p);
                }
            }
            default -> p.sendMessage(ChatColor.RED + "Feil arg, skriv inn [villager | rail]");
        }
        return true;
    }
}
