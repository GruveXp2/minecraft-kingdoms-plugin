package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Set;

public class SelectAddressMenu extends Menu {

    final Entrypoint ENTRYPOINT;

    public SelectAddressMenu(Entrypoint entrypoint) {
        ENTRYPOINT = entrypoint;
        updateItems();
    }

    @Override
    public String getMenuName() {
        return "Select address";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() > 8 || e.getCurrentItem().getType() == Material.BARRIER) {return;}
        ENTRYPOINT.setTargetAddress(e.getCurrentItem().getItemMeta().getDisplayName());
        ENTRYPOINT.openInventory(p, "main");
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(0, makeItem(Material.COMMAND_BLOCK, "Loading data..."));
    }

    public void updateItems() {
        District district = KingdomsManager.getKingdom(ENTRYPOINT.getTargetKingdom()).getDistrict(ENTRYPOINT.getTargetDistrict());
        Set<String> addressses = district.getAddressIDs();
        if (addressses.size() == 0) {
            inventory.setItem(0, makeItem(Material.BARRIER, ChatColor.RED + "This district has no addresses"));
        }
        int i = 0;
        for (String address : addressses) {
            inventory.setItem(i, makeItem(district.getAddress(address).getMaterial(), address));
            i++;
        }
    }
}
