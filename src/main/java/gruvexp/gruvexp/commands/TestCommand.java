package gruvexp.gruvexp.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
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

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.UUID;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

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
                    p.sendMessage("Skriv inn fler args. /test rail <cart uuid> <start kdom> <start distr> <section id> <dir> <kdom> <distr> <addr>");
                    return true;
                }
                CartManager.driveCart(UUID.fromString(args[1]), p, args[2], args[3], args[4], args[5].toCharArray()[0], args[6], args[7], args[8]);
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
            case "2" -> {
                PacketContainer rotationPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
                rotationPacket.getIntegers().write(0, p.getEntityId());
                rotationPacket.getBytes().write(0, (byte) 40);
                ProtocolManager manager = ProtocolLibrary.getProtocolManager();
                manager.broadcastServerPacket(rotationPacket);
            }
            case "3" -> {
                PacketContainer rotationPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
                rotationPacket.getIntegers().write(0, p.getEntityId());
                rotationPacket.getBytes().write(0, (byte) 40);
                ProtocolManager manager = ProtocolLibrary.getProtocolManager();
                try {
                    manager.sendServerPacket(p, rotationPacket);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
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
                    case "packetMount1" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.packetMount1(p, cart);
                    }
                    case "packetMount2" -> {
                        String id = args[2];
                        Minecart cart = (Minecart) Bukkit.getEntity(UUID.fromString(id));
                        Test.packetMount2(p, cart);
                    }
                    case "packetRotate" -> Test.packetRotate(p);
                }
            }
            default -> p.sendMessage(ChatColor.RED + "Feil arg, skriv inn [villager | rail]");
        }

        return true;
    }
}
