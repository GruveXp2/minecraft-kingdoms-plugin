package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.Entrypoint;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;

public class SelectDistrictMenu extends Menu {

    final Entrypoint entrypoint;

    public SelectDistrictMenu(Entrypoint entrypoint) {
        this.entrypoint = entrypoint;
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
        entrypoint.setTargetDistrict(entrypoint.getTargetKingdom().getDistrict(e.getCurrentItem().getItemMeta().getDisplayName()));
        entrypoint.openInventory(p, "main");
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(0, makeItem(Material.COMMAND_BLOCK, "Loading data..."));
    }

    public void updateItems() {
        Kingdom kingdom = entrypoint.getTargetKingdom();
        Collection<District> districts = kingdom.getDistricts();
        if (districts.isEmpty()) {
            inventory.setItem(0, makeItem(Material.BARRIER, ChatColor.RED + "This kingdom has no districts"));
        }
        int i = 0;
        for (District district : districts) {
            inventory.setItem(i, makeItem(district.getIcon(), district.id));
            i++;
        }
    }
}
