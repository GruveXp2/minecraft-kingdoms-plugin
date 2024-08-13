package gruvexp.gruvexp.rail;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
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

    public static void packetMount1(Player p, Minecart cart) {
        Location loc = p.getLocation();
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer mountPacket = manager.createPacket(PacketType.Play.Server.MOUNT); // a packet which makes the player mount the minecart
        mountPacket.getIntegers().write(0, cart.getEntityId());
        mountPacket.getIntegerArrays().write(0, new int[] {p.getEntityId()});
        manager.broadcastServerPacket(mountPacket);
    }

    public static void packetMount2(Player p, Minecart cart) {
        Location loc = p.getLocation();
        float yaw = loc.getYaw();
        float pitch = loc.getPitch();
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        PacketContainer mountPacket = manager.createPacket(PacketType.Play.Server.MOUNT); // a packet which makes the player mount the minecart
        mountPacket.getIntegers().write(0, cart.getEntityId());
        mountPacket.getIntegerArrays().write(0, new int[] {p.getEntityId()});
        manager.broadcastServerPacket(mountPacket);


        byte xRot = (byte) ((pitch / 360.0) * 256);
        byte yRot = (byte) ((yaw / 360.0) * 256);

        PacketContainer rotationPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        rotationPacket.getIntegers().write(0, p.getEntityId());
        rotationPacket.getBytes().write(0, yRot);
        rotationPacket.getBytes().write(1, xRot); // getBytes funker ikke for index 1, pga de er i samme byte?
        manager.broadcastServerPacket(rotationPacket);
    }

    public static void packetRotate(Player p) {
        Location loc = p.getLocation();
        float yaw = loc.getYaw() + 20;
        float pitch = loc.getPitch();
        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        byte xRot = (byte) ((pitch / 360.0) * 256);
        byte yRot = (byte) ((yaw / 360.0) * 256);

        PacketContainer rotationPacket = new PacketContainer(PacketType.Play.Server.ENTITY_LOOK);
        rotationPacket.getIntegers().write(0, p.getEntityId());
        rotationPacket.getBytes().write(0, yRot);
        rotationPacket.getBytes().write(1, xRot); // getBytes funker ikke for index 1, pga de er i samme byte?
        manager.broadcastServerPacket(rotationPacket);
    }

}
