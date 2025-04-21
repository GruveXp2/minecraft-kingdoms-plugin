package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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
        if (e.getRightClicked() instanceof ItemFrame frame) {
            if (frame.getItem().getType() != Material.COMMAND_BLOCK_MINECART) return;
            Set<String> tags = frame.getScoreboardTags();
            if (tags.size() != 1) return;

            Component result = handlePlayerItemFrameRightClick(p, frame);
            if (result != null) p.sendMessage(result);
        } else if (e.getRightClicked() instanceof Minecart cart) {
            Set<String> tags = cart.getScoreboardTags();
            if (tags.contains("running")) {return;}
            UUID cartUUID = cart.getUniqueId();
            if (!CartManager.isCartRegistered(cartUUID)) {
                if (tags.isEmpty()) {return;}
                for (String tag : tags) {
                    String[] address = tag.split("-");
                    Kingdom kingdom = KingdomsManager.getKingdom(address[0]);
                    if (kingdom == null) return;
                    District district = kingdom.getDistrict(address[1]);
                    if (district == null) return;
                    Locality locality = district.getLocality(address[2]);
                    if (locality == null) return;
                    CartManager.registerCart(cartUUID, locality);
                }
            } // hvis carten ikke er en cart spawna av stasjonen
            Locality locality = CartManager.getLocality(cartUUID);
            Entrypoint entrypoint = locality.getEntrypoint();
            if (entrypoint == null) {
                p.sendMessage(Component.text("This cart has a tag leading to an entrypoint which doesnt exist", NamedTextColor.RED));
                e.setCancelled(true);
                return;
            }
            if (entrypoint.getStationMenu().isMailMode()) {
                if (entrypoint.getTargetKingdom() == null) {
                    p.sendMessage(Component.text("You must set the kingdom before you send mail!", NamedTextColor.RED));
                    e.setCancelled(true);
                    return;
                }
            } else if (entrypoint.getTargetLocality() == null) {
                p.sendMessage(Component.text("You must set the address before you use the railway!", NamedTextColor.RED));
                e.setCancelled(true);
                return;
            }
            if (cart instanceof StorageMinecart) {
                e.setCancelled(true);
                if (!entrypoint.getStationMenu().isMailMode()) {
                    Main.getPlugin().getLogger().info("Clicked chest cart but was in drive mode, spawning drive cart");
                    cart.remove();
                    CartManager.removeCart(cartUUID);
                    entrypoint.spawnCart(EntityType.MINECART);
                    return;
                }
                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> entrypoint.getStationMenu().switchToDriveMode(), 10L);
            } else {
                if (entrypoint.getStationMenu().isMailMode()) {
                    Main.getPlugin().getLogger().info("Clicked drive cart but was in chest mode, spawning cest cart");
                    e.setCancelled(true);
                    cart.remove();
                    CartManager.removeCart(cartUUID);

                    entrypoint.spawnCart(EntityType.CHEST_MINECART);
                    return;
                }
            }
            CartManager.driveCart(cartUUID, p, entrypoint.getSection(), entrypoint.getDirection(), entrypoint.getTargetLocality());
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> entrypoint.spawnCart(EntityType.MINECART), 10L);
        }
    }

    private Component handlePlayerItemFrameRightClick(Player p, ItemFrame frame) {
        Set<String> tags = frame.getScoreboardTags();

        String tag = tags.iterator().next();
        String[] address = tag.split("-");

        String kingdomID = address[0];
        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
        if (kingdom == null) return Component.text("Malformed item frame tag! No kingdom named \"" + kingdomID + "\". Tag format must be <kingdom>:<district>:<locality>", NamedTextColor.RED);

        String districtID = address[1];
        District district = kingdom.getDistrict(districtID);
        if (district == null) return Component.text("Malformed item frame tag! No district named \"" + districtID + "\". Tag format must be <kingdom>:<district>:<locality>", NamedTextColor.RED);

        String localityID = address[2];
        Locality locality = district.getLocality(localityID);
        if (locality == null) return Component.text("Malformed item frame tag! No district named \"" + localityID + "\". Tag format must be <kingdom>:<district>:<locality>", NamedTextColor.RED);

        Entrypoint entrypoint = locality.getEntrypoint();
        if (entrypoint == null) return Component.text("No entrypoint is attached to this locality. Run /locality <locality> add entrypoint to add one", NamedTextColor.YELLOW);
        entrypoint.openInventory(p, "main");
        return null;
    }
}
