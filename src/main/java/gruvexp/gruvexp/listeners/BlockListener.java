package gruvexp.gruvexp.listeners;

import gruvexp.gruvexp.nyttår.NyttårCommand;
import gruvexp.gruvexp.nyttår.Year2025;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (NyttårCommand.settingBlocks) {
            if (!p.getName().equals("GruveXp")) return;
            Block block = e.getBlock();
            Location loc = block.getLocation();
            Material material = block.getType();

            if (material.name().endsWith("_CONCRETE")) {
                String glass = material.name().replace("_CONCRETE", "_STAINED_GLASS");
                Material stainedGlass = Material.valueOf(glass);
                System.out.println("Konvertert: " + material.name() + " til " + stainedGlass.name());
                block.setType(stainedGlass);
                e.setCancelled(true);
                Year2025.addSnøFnuggBlock(loc);
            }
        } else if (NyttårCommand.settingNumberBlocks) {
            if (!p.getName().equals("GruveXp")) return;
            Location loc = e.getBlock().getLocation();
            e.setCancelled(true);
            Year2025.addNumberNode(NyttårCommand.isSetting20, loc);
        }
    }
}
