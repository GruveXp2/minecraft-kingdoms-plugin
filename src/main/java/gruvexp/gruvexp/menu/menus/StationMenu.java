package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.CartManager;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.core.KingdomsManager;
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

    final Entrypoint ENTRYPOINT; // refrence til entrypoint
    boolean chestMode = false; // false -> drivemode

    public StationMenu(Entrypoint entrypoint) {
        this.ENTRYPOINT = entrypoint;
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
        Minecart new_cart = (Minecart) Main.WORLD.spawnEntity(ENTRYPOINT.getCoord().toLocation(Main.WORLD), entityType);
        registerCart(new_cart);
    }

    private void registerCart(Minecart cart) {
        String[] fullAddress = ENTRYPOINT.getFullAddress();
        CartManager.registerCart(cart.getUniqueId(), fullAddress);
        cart.addScoreboardTag(fullAddress[0] + "-" + fullAddress[1] + "-" + fullAddress[2]);
        ENTRYPOINT.setCartUUID(cart.getUniqueId());
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getSlot() >= getSlots()) {return;}
        Player p = (Player) e.getWhoClicked(); // send player til selectkingdom menu osv.
        switch (ChatColor.stripColor(e.getCurrentItem().getItemMeta().getLore().get(0))) {
            case "kingdom" -> ENTRYPOINT.openInventory(p, "kingdom");
            case "district" -> ENTRYPOINT.openInventory(p, "district");
            case "address" -> ENTRYPOINT.openInventory(p, "address");
            case "respawn" -> spawnCart(EntityType.MINECART);
            case "drive" -> { // change 2 messidge mode
                // fjerner carten på stasjonen hvis den er der
                for (Entity entity : Main.WORLD.getNearbyEntities(KingdomsManager.getKingdom(ENTRYPOINT.getKingdomID()).getDistrict(ENTRYPOINT.getDistrictID()).getSection(ENTRYPOINT.getSectionID()).getEntry().toLocation(Main.WORLD), 1, 1, 1)) {
                    if (!(entity instanceof Minecart)) {continue;}
                    if ((entity instanceof StorageMinecart)) {
                        registerCart((Minecart) entity);
                        continue;
                    }
                    entity.remove();
                }
                chestMode();
            }
            case "message" -> { // change 2 drive mode
                for (Entity entity : Main.WORLD.getNearbyEntities(KingdomsManager.getKingdom(ENTRYPOINT.getKingdomID()).getDistrict(ENTRYPOINT.getDistrictID()).getSection(ENTRYPOINT.getSectionID()).getEntry().toLocation(Main.WORLD), 1, 1, 1)) {
                    if (!(entity instanceof Minecart)) {continue;}
                    if ((entity instanceof RideableMinecart)) {
                        registerCart((Minecart) entity);
                        continue;
                    }
                    entity.remove();
                }
                driveMode();
            }
            case "edit the contents" -> {
                StorageMinecart cart = (StorageMinecart) Bukkit.getEntity(ENTRYPOINT.getCartUUID());
                if (cart == null) {
                    p.sendMessage(ChatColor.RED + "Couldnt find the chest minecart! Spawning new one...");
                    spawnCart(EntityType.MINECART_CHEST);
                    cart = (StorageMinecart) Bukkit.getEntity(ENTRYPOINT.getCartUUID());
                }
                p.openInventory(cart.getInventory());
            }
        }
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(1, makeItem(Material.PAPER, "Select kingdom", "kingdom"));
        inventory.setItem(7, makeItem(Material.MINECART, "Respawn cart", "respawn"));
        inventory.setItem(8, makeItem(Material.MINECART, "Mode", "drive"));
    }

    public void setKingdom(String kingdomID) {
        Kingdom kingdom = KingdomsManager.getKingdom(ENTRYPOINT.getTargetKingdom());
        String player = kingdom.getPlayer();
        if (!chestMode) {
            inventory.setItem(3, makeItem(Material.PAPER, "select district", "district"));
        }
        inventory.setItem(5, null);
        if (chestMode) {
            ENTRYPOINT.setTargetDistrict(kingdom.getPostOfficeDistrict());
            ENTRYPOINT.setTargetAddress("post_office");
        }
        inventory.setItem(1, makeHeadItem(Bukkit.getOfflinePlayer(player), kingdomID, "kingdom", player));

    }

    public void setDistrict(String districtID) {
        District district = KingdomsManager.getKingdom(ENTRYPOINT.getTargetKingdom()).getDistrict(districtID);
        if (!chestMode) {
            inventory.setItem(3, makeItem(district.getMaterial(), districtID, "district"));
            inventory.setItem(5, makeItem(Material.PAPER, "select address", "address"));
        }
    }

    public void setAddress(String address) {
        District district = KingdomsManager.getKingdom(ENTRYPOINT.getTargetKingdom()).getDistrict(ENTRYPOINT.getTargetDistrict());
        if (!chestMode) {
            inventory.setItem(5, makeItem(district.getAddress(address).getMaterial(), address, "address"));
        }
    }

    public void driveMode() {
        chestMode = false;
        inventory.setItem(7, makeItem(Material.MINECART, "Respawn cart", "respawn"));
        inventory.setItem(8, makeItem(Material.MINECART, "Mode", ChatColor.BLUE + "drive"));
        Minecart prevCart = null;
        try {
            prevCart = (Minecart) Bukkit.getEntity(ENTRYPOINT.getCartUUID());
        } catch (IllegalArgumentException ignored) {

        }
        if (prevCart == null || prevCart instanceof RideableMinecart ) {
            spawnCart(EntityType.MINECART);
        }
    }

    public void chestMode() {
        chestMode = true;
        inventory.setItem(3, null);
        inventory.setItem(5, null);
        inventory.setItem(7, makeItem(Material.CHEST, "Edit contents", "edit the contents", "of the minecart"));
        inventory.setItem(8, makeItem(Material.CHEST_MINECART, "Mode", ChatColor.GOLD + "message"));
        try {
            Kingdom kingdom = KingdomsManager.getKingdom(ENTRYPOINT.getTargetKingdom());
            ENTRYPOINT.setTargetDistrict(kingdom.getPostOfficeDistrict());
            ENTRYPOINT.setTargetAddress("post_office");
        } catch (IllegalArgumentException ignored) {
            ENTRYPOINT.resetAddress();
            inventory.setItem(1, makeItem(Material.PAPER, "Select kingdom", "kingdom"));
        }
        Minecart prevCart = null;
        try {
            prevCart = (Minecart) Bukkit.getEntity(ENTRYPOINT.getCartUUID());
        } catch (IllegalArgumentException ignored) {

        }
        if (prevCart == null || prevCart instanceof RideableMinecart ) {
            spawnCart(EntityType.MINECART_CHEST);
        }
    }

    public boolean isChestMode() {
        return chestMode;
    }
}