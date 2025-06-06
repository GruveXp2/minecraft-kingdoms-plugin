package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.core.KingdomsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;

public class SelectKingdomMenu extends Menu {

    final Entrypoint entrypoint;

    public SelectKingdomMenu(Entrypoint entrypoint) {
        this.entrypoint = entrypoint;
    }

    @Override
    public String getMenuName() {
        return "Select kingdom";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        if (e.getSlot() >= getSlots()) {return;}
        Player p = (Player) e.getWhoClicked();
        if (e.getSlot() > 8) {return;}
        Component result = entrypoint.setTargetKingdom(KingdomsManager.getKingdom(e.getCurrentItem().getItemMeta().getDisplayName()));
        if (result != null) p.sendMessage(result);
        entrypoint.openInventory(p, "main");
    }

    @Override
    public void setMenuItems() {
        Collection<Kingdom> kingdoms = KingdomsManager.getKingdoms();
        int i = 0;
        for (Kingdom kingdom : kingdoms) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(kingdom.getKingID());
            inventory.setItem(i, makeHeadItem(p, kingdom.id, p.getName()));
            i++;
        }
    }
}
