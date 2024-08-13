package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

import java.util.Set;
import java.util.UUID;

public class RailCartListener implements Listener {

    // når man trykker på item frame med commandblock minecart så åpnes menu
    @EventHandler
    public void onRailInteract(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (e.getRightClicked() instanceof ItemFrame) {
            ItemFrame frame = (ItemFrame) e.getRightClicked();
            if (frame.getItem().getType() != Material.COMMAND_BLOCK_MINECART) {return;}
            Set<String> tags = frame.getScoreboardTags();
            if (tags.size() != 1) {return;}
            String tag = "";
            for (String s : tags) {
                tag = s;
            }
            String[] tag_data = tag.split("-");
            Entrypoint entrypoint;
            try {
                entrypoint = KingdomsManager.getKingdom(tag_data[0]).getDistrict(tag_data[1]).getEntrypoint(tag_data[2]); // hvis framen ikke er en frame fra en stasjon
            } catch (NullPointerException ex) {
                p.sendMessage(ChatColor.RED + "Invalid tag for control panel: " + tag);
                return;
            }
            if (entrypoint == null) {
                p.sendMessage(ChatColor.RED + "No entrypoint is attached to this address");
                return;
            }
            entrypoint.openInventory(p, "main");
        } else if (e.getRightClicked() instanceof Minecart) {
            Minecart cart = (Minecart) e.getRightClicked();
            Set<String> tags = cart.getScoreboardTags();
            if (tags.contains("running")) {return;}
            UUID cartUUID = cart.getUniqueId();
            if (!CartManager.isCartRegistered(cartUUID)) {
                if (tags.size() == 0) {return;}
                for (String tag : tags) {
                    String[] tagData = tag.split("-");
                    CartManager.registerCart(cartUUID, new String[]{tagData[0], tagData[1], tagData[2]});
                }
            } // hvis carten ikke er en cart spawna av stasjonen
            String[] address = CartManager.getFullAddress(cartUUID);
            Entrypoint entrypoint = KingdomsManager.getKingdom(address[0]).getDistrict(address[1]).getEntrypoint(address[2]);
            if (entrypoint.getTargetAddress() == null) {
                p.sendMessage(ChatColor.RED + "You must set the address before you use the railway");
                e.setCancelled(true);
                return;
            }
            if (cart instanceof StorageMinecart) {
                e.setCancelled(true);
                if (!entrypoint.getStationMenu().isChestMode()) {
                    Bukkit.getLogger().info("Clicked chest cart but was in drive mode, spawning drive cart");
                    cart.remove();
                    CartManager.removeCart(cartUUID);
                    spawnCart(entrypoint, address, EntityType.MINECART);
                    return;
                }
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> entrypoint.getStationMenu().driveMode(), 10L);
            } else {
                if (entrypoint.getStationMenu().isChestMode()) {
                    Bukkit.getLogger().info("Clicked drive cart but was in chest mode, spawning cest cart");
                    e.setCancelled(true);
                    cart.remove();
                    CartManager.removeCart(cartUUID);
                    //spawner ny cart
                    spawnCart(entrypoint, address, EntityType.MINECART_CHEST);
                    return;
                }
            }
            CartManager.driveCart(cartUUID, p, address[0], address[1], entrypoint.getSectionID(), entrypoint.getDirection(), entrypoint.getTargetKingdom(), entrypoint.getTargetDistrict(), entrypoint.getTargetAddress());
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> spawnCart(entrypoint, address, EntityType.MINECART), 10L);
        }
    }

    public void spawnCart(Entrypoint entrypoint, String[] address, EntityType entityType) {
        Minecart new_cart = (Minecart) Main.WORLD.spawnEntity(entrypoint.getCoord().toLocation(Main.WORLD), entityType);
        CartManager.registerCart(new_cart.getUniqueId(), address);
        new_cart.addScoreboardTag(address[0] + "-" + address[1] + "-" + address[2]);
    }
}
