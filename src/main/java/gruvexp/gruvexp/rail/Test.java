package gruvexp.gruvexp.rail;

import org.bukkit.Location;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class Test {

    public static void unMountCart(Player p, Minecart cart) {
        if (cart.getPassengers().contains(p)) {
            cart.removePassenger(p);
            p.sendMessage("You left the cart");
        }
    }

    public static void mountCart(Player p, Minecart cart) {
        if (!cart.getPassengers().contains(p)) {
            cart.addPassenger(p);
            p.sendMessage("You mounted the cart");
        }
    }

    public static void rotate1(Player p) {
        Location loc = p.getLocation();
        loc.setYaw(loc.getYaw()+20);
        p.teleport(loc);
    }

    public static void rotate2(Player p, Minecart minecart) {
        Location loc = p.getLocation();
        loc.setYaw(loc.getYaw()+20);
        minecart.teleport(loc);
        p.teleport(minecart);
    }

    public static void rotate3(Player p, Minecart minecart) {
        Location loc = p.getLocation();
        loc.setYaw(loc.getYaw()+20);
        minecart.teleport(loc);
        minecart.addPassenger(p);
    }

    public static void rotate4(Player p) {
        Location loc = p.getLocation();
        p.setRotation(loc.getYaw()+20, loc.getPitch());
    }
}
