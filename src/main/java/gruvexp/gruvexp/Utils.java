package gruvexp.gruvexp;

import gruvexp.gruvexp.rail.Coord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.io.*;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Utils {
    public static String loadTxt(String fileName) { //returner string som er hele dokumentet
        StringBuilder input = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\gruve\\Desktop\\Server\\Four Kingdoms\\plugin data\\"+fileName+".txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                input.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return input.toString();
    }
    public static void saveTxt(String fileName, String fileData) { //returner string som er hele dokumentet
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\gruve\\Desktop\\Server\\Four Kingdoms\\plugin data\\"+fileName+".txt"));
            writer.write(fileData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Coord getPlayerBlockCoords(Player p) {
        Location loc = p.getLocation();
        int pos_x = (int) loc.getX();
        if (pos_x < 0) {pos_x -= 1;}
        int pos_y = (int) loc.getY();
        if (pos_y < 0) {pos_y -= 1;}
        int pos_z = (int) loc.getZ();
        if (pos_z < 0) {pos_z -= 1;}
        return new Coord(pos_x, pos_y, pos_z);
    }

    public static Material getMaterial(String namespaced) {
        Material material = Material.getMaterial(namespaced.toUpperCase());
        if (material == null) {
            throw new IllegalArgumentException(ChatColor.RED + "\"" + namespaced + "\" is an invalid material!");
        }
        return material;
    }

    public static Coord getTargetBlock(Player player, int range) { // modified method by https://www.spigotmc.org/members/clip.1001/ that gets the block the player is looking at
        BlockIterator iter = new BlockIterator(player, range);
        Block lastBlock = iter.next();
        while (iter.hasNext()) {
            lastBlock = iter.next();
            if (lastBlock.getType() == Material.AIR) {
                continue;
            }
            break;
        }
        return new Coord(lastBlock.getX(), lastBlock.getY(), lastBlock.getZ());
    }

    public static String ToName(String str) {
        return Arrays.stream(str.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static void openDoor(Block doorBlock) {
        BlockData data = doorBlock.getBlockData();

        if (data instanceof Door) {
            Door door = (Door) data;
            door.setOpen(true);
            doorBlock.setBlockData(door);
        }
    }
}
