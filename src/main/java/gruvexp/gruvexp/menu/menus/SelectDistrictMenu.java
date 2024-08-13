package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Set;

public class SelectDistrictMenu extends Menu {

    final Entrypoint ENTRYPOINT;

    public SelectDistrictMenu(Entrypoint entrypoint) {
        ENTRYPOINT = entrypoint;
        updateItems();
    }

    @Override
    public String getMenuName() {
        return "Select district";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() > 8 || e.getCurrentItem().getType() == Material.BARRIER) {return;}
        ENTRYPOINT.setTargetDistrict(e.getCurrentItem().getItemMeta().getDisplayName());
        ENTRYPOINT.openInventory(p, "main");
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(0, makeItem(Material.COMMAND_BLOCK, "Loading data..."));
    }

    public void updateItems() {
        Kingdom kingdom = KingdomsManager.getKingdom(ENTRYPOINT.getTargetKingdom());
        Set<String> districts = kingdom.getDistrictIDs();
        if (districts.size() == 0) {
            inventory.setItem(0, makeItem(Material.BARRIER, ChatColor.RED + "This kingdom has no districts"));
        }
        int i = 0;
        for (String district : districts) {
            inventory.setItem(i, makeItem(kingdom.getDistrict(district).getMaterial(), district));
            i++;
        }
    }
}
