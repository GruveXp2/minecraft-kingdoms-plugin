package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.rail.Coord;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

public class ShrinkCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player p = (Player) sender;
        if (args.length < 10) {
            p.sendMessage(ChatColor.RED + "Not enough args!\n" + ChatColor.WHITE + "Usage: " + command.getUsage());
            return true;
        }
        try {
            Coord coord1 = new Coord(args[0], args[1], args[2]);
            Coord coord2 = new Coord(args[3], args[4], args[5]);
            Coord coordTo = new Coord(args[6], args[7], args[8]);
            float size = 0f;
            try {
                if (args[9].contains("/")) {
                    String[] sizeExpression = args[9].split("/");
                    size = Float.parseFloat(sizeExpression[0]) / Integer.parseInt(sizeExpression[1]);
                } else {
                    size = Float.parseFloat(args[9]);
                }
            } catch (NumberFormatException e) {
                p.sendMessage(ChatColor.RED + "Size must be a number");
            }
            Coord coordFrom = new Coord(Math.min(coord1.getX(), coord2.getX()), Math.min(coord1.getY(), coord2.getY()), Math.min(coord1.getZ(), coord2.getZ()));
            Coord dimensions = new Coord(Math.abs(coord1.getX() - coord2.getX()), Math.abs(coord1.getY() - coord2.getY()), Math.abs(coord1.getZ() - coord2.getZ()));
            if (dimensions.getX() * dimensions.getY() * dimensions.getZ() > 16384) {
                p.sendMessage(ChatColor.RED + "Too many blocks to clone! (the server cant handle that many)");
                return true;
            }
            String tag = null;
            if (args.length == 11) {
                tag = args[10];
            }
            World world = p.getWorld();
            for (int x = 0; x < dimensions.getX() + 1; x++) {
                for (int y = 0; y < dimensions.getY() + 1; y++) {
                    for (int z = 0; z < dimensions.getZ() + 1; z++) {
                        Block block = world.getBlockAt(coordFrom.getX() + x, coordFrom.getY() + y, coordFrom.getZ() + z);
                        if (block.getType() != Material.AIR) {
                            BlockDisplay display = (BlockDisplay) world.spawnEntity(new Location(world, coordTo.getX() + x * size, coordTo.getY() + y * size, coordTo.getZ() + z * size), EntityType.BLOCK_DISPLAY);
                            BlockData blockData = block.getBlockData();
                            display.setBlock(blockData);
                            display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(size, size, size), new AxisAngle4f(0, 0, 0, 1)));
                            if (tag != null) {
                                display.addScoreboardTag(tag);
                            }
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            p.sendMessage(e.getMessage());
        }
        return true;
    }
}
