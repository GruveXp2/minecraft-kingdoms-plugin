package gruvexp.gruvexp;

import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
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
            BufferedReader reader = new BufferedReader(new FileReader(FilePath.SERVER_FOLDER + FilePath.SERVER_NAME + "\\plugin data\\"+fileName+".txt"));
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
            BufferedWriter writer = new BufferedWriter(new FileWriter(FilePath.SERVER_FOLDER + FilePath.SERVER_NAME + "\\plugin data\\"+fileName+".txt"));
            writer.write(fileData);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Coord getPlayerBlockCoords(Player p) {
        Location loc = p.getLocation();
        int posX = loc.getBlockX();
        int posY = loc.getBlockY();
        int posZ = loc.getBlockZ();
        return new Coord(posX, posY, posZ);
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

    public static String toName(String id) {
        return Arrays.stream(id.split("_"))
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String toID(String name) {
        return name.toLowerCase().replace(" ", "_");
    }

    public static void openDoor(Block doorBlock) {
        BlockData data = doorBlock.getBlockData();

        if (data instanceof Door door) {
            door.setOpen(true);
            doorBlock.setBlockData(door);
        }
    }

    public static Component location(Location loc) {
        return Component.text(String.format("%.2f %.0f %.2f", loc.getX(), loc.getY(), loc.getZ()), NamedTextColor.BLUE);
    }
}
