package gruvexp.gruvexp.rail;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class CalculateLength extends BukkitRunnable {

    int counter = 0;
    char direction; // hvilken retning carten kjører. når man kommer til svinger vil den nye retninga avhenge av den forrige retninga
    final CommandMinecart cart;
    final Section section;
    final Location loc;
    final Location exit;
    final Player p;
    final int startDistance;
    HashMap<Location, Integer> speedPositions;
    boolean doubleRun = false; // system som gjør at hver tick flytter han seg 2 hakk
    public CalculateLength(Section section, char direction, Player p) {
        this.section = section;
        this.direction = direction;
        this.p = p;
        this.speedPositions = section.getSpeedPositions();
        Coord entry = section.getEntry();
        World world = p.getWorld();
        loc = new Location(world, entry.x() + 0.5, entry.y(), entry.z() + 0.5);
        cart = (CommandMinecart) world.spawnEntity(loc, EntityType.COMMAND_BLOCK_MINECART);
        Coord exit = section.getExit();
        this.exit = new Location(world, exit.x() + 0.5, exit.y(), exit.z() + 0.5);
        startDistance = (int) Math.sqrt(Math.pow(entry.x() - exit.x(), 2) + Math.pow(entry.y() - exit.y(), 2) + Math.pow(entry.z() - exit.z(), 2));
        p.sendMessage("Calculating...");
    }


    @Override
    public void run() {
        if (speedPositions.containsKey(loc)) {
            int speed = speedPositions.get(loc);
            section.setSpeed(counter, speed);
            p.sendMessage(Component.text("Speed change at " + counter + ": ").append(section.speed(speed)));
        }
        counter++;
        if (counter % 50 == 0) {
            int newDistance = (int) Math.sqrt(Math.pow(loc.getX() - exit.getX(), 2) + Math.pow(loc.getY() - exit.getY(), 2) + Math.pow(loc.getZ() - exit.getZ(), 2));
            int percent = (newDistance * 100) / startDistance;
            p.sendMessage((100 - percent) + "%");
        }
        Material material = loc.getBlock().getType();
        if (material == Material.AIR) {
            Material material2 = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ()).getBlock().getType();
            if (material2 == Material.RAIL || material2 == Material.POWERED_RAIL) {
                loc.add(0, -1, 0);
            } else {
                p.sendMessage(ChatColor.RED + "Cart derailed, length calculation cancelled");
                cancel();
                return;
            }
        } else {
            if (material != Material.RAIL && material != Material.POWERED_RAIL) {
                p.sendMessage(ChatColor.RED + "Cart derailed, length calculation cancelled");
                cancel();
                return;
            }
            Rail data = (Rail) loc.getBlock().getBlockData();
            Rail.Shape shape = data.getShape();
            switch (direction) {
                case 'n' -> {
                    switch (shape) {
                        case SOUTH_EAST -> direction = 'e'; // 2 måter å svinge, ellers går den bare rett fram
                        case SOUTH_WEST -> direction = 'w';
                        case ASCENDING_NORTH -> loc.add(0, 1, 0); // hvis det er bakke så telporteres den opp/ned i tilegg
                        case ASCENDING_SOUTH -> loc.add(0, -1, 0);
                    }
                }
                case 's' -> {
                    switch (shape) {
                        case NORTH_EAST -> direction = 'e';
                        case NORTH_WEST -> direction = 'w';
                        case ASCENDING_NORTH -> loc.add(0, -1, 0);
                        case ASCENDING_SOUTH -> loc.add(0, 1, 0);
                    }
                }
                case 'e' -> {
                    switch (shape) {
                        case NORTH_WEST -> direction = 'n';
                        case SOUTH_WEST -> direction = 's';
                        case ASCENDING_EAST -> loc.add(0, 1, 0);
                        case ASCENDING_WEST -> loc.add(0, -1, 0);
                    }
                }
                case 'w' -> {
                    switch (shape) {
                        case NORTH_EAST -> direction = 'n';
                        case SOUTH_EAST -> direction = 's';
                        case ASCENDING_EAST -> loc.add(0, -1, 0);
                        case ASCENDING_WEST -> loc.add(0, 1, 0);
                    } // ellers er direction den samme
                }
            }
        }
        switch (direction) { // endrer loc i den retninga som er "direction"
            case 'n' -> loc.add(0, 0, -1);
            case 's' -> loc.add(0, 0, 1);
            case 'e' -> loc.add(1, 0, 0);
            case 'w' -> loc.add(-1, 0, 0);
        }
        cart.teleport(loc);
        if (loc.equals(exit)) {
            section.setLength(counter);
            p.sendMessage("100% - Rail length: " + ChatColor.AQUA + counter + "m");
            cart.remove();
            cancel();
        }
        doubleRun = !doubleRun;
        if (doubleRun) {
            run();
        }
    }
}
