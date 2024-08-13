package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.Section;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.*;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.scheduler.BukkitRunnable;

public class CalculateLength extends BukkitRunnable {

    int counter = 0;
    char direction; // hvilken retning carten kjører. når man kommer til svinger vil den nye retninga avhenge av den forrige retninga
    final CommandMinecart cart;
    String sectionID;
    Location loc;
    Location exit;
    Player p;
    int start_distance;
    District district;
    boolean doubleRun = false; // system som gjør at hver tick flytter han seg 2 hakk
    public CalculateLength(String kingdomID, String districtID, String sectionID, char direction, Player p) {
        this.sectionID = sectionID;
        this.direction = direction;
        this.p = p;
        district = KingdomsManager.getKingdom(kingdomID).getDistrict(districtID);
        Section section = district.getSection(sectionID);
        Coord coord = section.getEntry();
        World world = p.getWorld();
        loc = new Location(world, coord.getX() + 0.5, coord.getY(), coord.getZ() + 0.5);
        cart = (CommandMinecart) world.spawnEntity(loc, EntityType.MINECART_COMMAND);
        Coord exit_coord = section.getExit();
        exit = new Location(world, exit_coord.getX() + 0.5, exit_coord.getY(), exit_coord.getZ() + 0.5);
        start_distance = (int) Math.sqrt(Math.pow(coord.getX() - exit_coord.getX(), 2) + Math.pow(coord.getY() - exit_coord.getY(), 2) + Math.pow(coord.getZ() - exit_coord.getZ(), 2));
        p.sendMessage("Calculating...");
    }


    @Override
    public void run() {
        counter++;
        if (counter % 50 == 0) {
            int new_distance = (int) Math.sqrt(Math.pow(loc.getX() - exit.getX(), 2) + Math.pow(loc.getY() - exit.getY(), 2) + Math.pow(loc.getZ() - exit.getZ(), 2));
            int percent = (new_distance * 100) / start_distance;
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
            district.getSection(sectionID).setLength(counter);
            p.sendMessage("100% - Rail length: " + ChatColor.AQUA + counter + "m");
            KingdomsManager.save = true;
            cart.remove();
            cancel();
        }
        doubleRun = !doubleRun;
        if (doubleRun) {
            run();
        }
    }
}
