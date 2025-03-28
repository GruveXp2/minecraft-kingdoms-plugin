package gruvexp.gruvexp.nyttår;


import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class Forskerlinja {

    public static Location location = null;

    public static void makeBlocks(Player p) {

        int Δx = 125;
        int Δy = 34;
        Location pLoc = p.getLocation();
        float size = 0.8f;

        World world = p.getWorld();
        for (int x = 0; x < Δx + 1; x++) {
            for (int y = 0; y < Δy + 1; y++) {
                Block block = world.getBlockAt(pLoc.getBlockX() + x, pLoc.getBlockY() - y, pLoc.getBlockZ() + 2);
                if (block.getType() != Material.AIR) {
                    BlockDisplay display = (BlockDisplay) world.spawnEntity(block.getLocation(), EntityType.BLOCK_DISPLAY);
                    BlockData blockData = block.getBlockData();
                    display.setBlock(blockData);
                    display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(size, size, size), new AxisAngle4f(0, 0, 0, 1)));
                    display.addScoreboardTag("forskerlinja");
                }
            }
        }
    }

}
