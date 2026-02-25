package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.Entrypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;

public class SelectLocalityMenu extends Menu {

    final Entrypoint entrypoint;

    public SelectLocalityMenu(Entrypoint entrypoint) {
        this.entrypoint = entrypoint;
        updateItems();
    }

    @Override
    public Component getMenuName() {
        return Component.text("Select locality");
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() > 8 || e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.BARRIER) {return;}
        entrypoint.setTargetLocality(entrypoint.getTargetDistrict().getLocality(e.getCurrentItem().getItemMeta().getDisplayName()));
        entrypoint.openInventory(p, "main");
    }

    @Override
    public void setMenuItems() {
        inventory.setItem(0, makeItem(Material.COMMAND_BLOCK, "Loading data..."));
    }

    public void updateItems() {
        District district = entrypoint.getTargetDistrict();
        Collection<Locality> localities = district.getLocalities();
        if (localities.isEmpty()) {
            inventory.setItem(0, makeItem(Material.BARRIER, Component.text("This district has no localities", NamedTextColor.RED)));
        }
        int i = 0;
        for (Locality locality : localities) {
            inventory.setItem(i, makeItem(locality.getIcon(), locality.id));
            i++;
        }
    }
}
