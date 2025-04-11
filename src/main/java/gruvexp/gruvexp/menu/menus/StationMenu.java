package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.CartManager;
import gruvexp.gruvexp.rail.Entrypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.entity.minecart.StorageMinecart;
import org.bukkit.event.inventory.InventoryClickEvent;

public class StationMenu extends Menu {

    final Entrypoint entrypoint; // refrence til entrypoint
    boolean mailMode = false; // false -> drivemode

    public StationMenu(Entrypoint entrypoint) {
        this.entrypoint = entrypoint;
    }

    @Override
    public String getMenuName() {
        return "Select Destination";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    public void spawnCart(EntityType entityType) {
        Minecart newCart = (Minecart) Main.WORLD.spawnEntity(entrypoint.getCoord().toLocation(Main.WORLD), entityType);
        registerCart(newCart);
    }

    private void registerCart(Minecart cart) {
        Locality locality = entrypoint.getLocality();
        CartManager.registerCart(cart.getUniqueId(), entrypoint.getLocality());
        cart.addScoreboardTag(locality.tag());
        entrypoint.setCartUUID(cart.getUniqueId());
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getSlot() >= getSlots()) {return;}
        Player p = (Player) e.getWhoClicked(); // send player til selectkingdom menu osv.
        switch (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().getFirst())) {
            case "kingdom" -> entrypoint.openInventory(p, "kingdom");
            case "district" -> entrypoint.openInventory(p, "district");
            case "locality" -> entrypoint.openInventory(p, "locality");
            case "respawn" -> spawnCart(EntityType.MINECART);
            case "drive" -> { // change 2 messidge mode
                // fjerner carten på stasjonen hvis den er der
                for (Entity entity : Main.WORLD.getNearbyEntities(entrypoint.getSection().getEntry().toLocation(Main.WORLD), 1, 1, 1)) {
                    if (!(entity instanceof Minecart)) {continue;}
                    if ((entity instanceof StorageMinecart)) {
                        registerCart((Minecart) entity);
                        continue;
                    }
                    entity.remove();
                }
                switchToMailMode();
            }
            case "message" -> { // change 2 drive mode
                for (Entity entity : Main.WORLD.getNearbyEntities(entrypoint.getSection().getEntry().toLocation(Main.WORLD), 1, 1, 1)) {
                    if (!(entity instanceof Minecart)) {continue;}
                    if ((entity instanceof RideableMinecart)) {
                        registerCart((Minecart) entity);
                        continue;
                    }
                    entity.remove();
                }
                switchToDriveMode();
            }
            case "edit the contents" -> {
                StorageMinecart storageCart = (StorageMinecart) Bukkit.getEntity(entrypoint.getCartUUID());
                if (storageCart == null) {
                    p.sendMessage(Component.text("Couldnt find the chest minecart! Spawning new one...", NamedTextColor.RED));
                    spawnCart(EntityType.CHEST_MINECART);
                    storageCart = (StorageMinecart) Bukkit.getEntity(entrypoint.getCartUUID());
                }
                p.openInventory(storageCart.getInventory());
            }
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(1, makeItem(Material.PAPER, "Select kingdom", "kingdom"));
        inventory.setItem(7, makeItem(Material.MINECART, "Respawn cart", "respawn"));
        inventory.setItem(8, makeItem(Material.MINECART, "Mode", "drive"));
    }

    public void setKingdom(Kingdom kingdom) {
        Player p = Bukkit.getPlayer(kingdom.getKingID());
        if (!mailMode) {
            inventory.setItem(3, makeItem(Material.PAPER, "select district", "district"));
        }
        inventory.setItem(5, null);
        if (mailMode) {
            District postOfficeDistrict = kingdom.getPostOfficeDistrict();
            Locality targetLocality = postOfficeDistrict.getLocality("post_office");
            if (targetLocality == null) return;
            entrypoint.setTargetLocality(targetLocality);
        }
        inventory.setItem(1, makeHeadItem(p, kingdom.id, "kingdom", p.getName()));
    }

    public void setDistrict(District district) {
        if (!mailMode) {
            inventory.setItem(3, makeItem(district.getIcon(), district.id, "district"));
            inventory.setItem(5, makeItem(Material.PAPER, "select address", "address"));
        }
    }

    public void setLocality(Locality locality) {
        if (!mailMode) {
            inventory.setItem(5, makeItem(locality.getIcon(), locality.id, "address"));
        }
    }

    public void switchToDriveMode() {
        mailMode = false;
        inventory.setItem(7, makeItem(Material.MINECART, "Respawn cart", "respawn"));
        inventory.setItem(8, makeItem(Material.MINECART, "Mode", ChatColor.BLUE + "drive"));
        Minecart prevCart = null;
        try {
            prevCart = (Minecart) Bukkit.getEntity(entrypoint.getCartUUID());
        } catch (IllegalArgumentException ignored) {

        }
        if (prevCart == null || prevCart instanceof RideableMinecart ) {
            spawnCart(EntityType.MINECART);
        }
    }

    public void switchToMailMode() {
        mailMode = true;
        inventory.setItem(3, null);
        inventory.setItem(5, null);
        inventory.setItem(7, makeItem(Material.CHEST, "Edit contents", "edit the contents", "of the minecart"));
        inventory.setItem(8, makeItem(Material.CHEST_MINECART, "Mode", ChatColor.GOLD + "message"));
        try {
            District targetDistrict = entrypoint.getTargetKingdom().getPostOfficeDistrict();
            entrypoint.setTargetDistrict(targetDistrict);
            entrypoint.setTargetLocality(targetDistrict.getLocality("post_office"));
        } catch (IllegalArgumentException ignored) {
            entrypoint.resetAddress();
            inventory.setItem(1, makeItem(Material.PAPER, "Select kingdom", "kingdom"));
        }
        Minecart prevCart = null;
        try {
            prevCart = (Minecart) Bukkit.getEntity(entrypoint.getCartUUID());
        } catch (IllegalArgumentException ignored) {

        }
        if (prevCart == null || prevCart instanceof RideableMinecart ) {
            spawnCart(EntityType.CHEST_MINECART);
        }
    }

    public boolean isMailMode() {
        return mailMode;
    }
}