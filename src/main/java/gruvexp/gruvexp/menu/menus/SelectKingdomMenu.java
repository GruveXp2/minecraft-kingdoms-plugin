package gruvexp.gruvexp.menu.menus;

import gruvexp.gruvexp.menu.Menu;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Set;

public class SelectKingdomMenu extends Menu {

    final Entrypoint ENTRYPOINT;

    public SelectKingdomMenu(Entrypoint entrypoint) {
        ENTRYPOINT = entrypoint;
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
        try {
            ENTRYPOINT.setTargetKingdom(e.getCurrentItem().getItemMeta().getDisplayName());
        } catch (IllegalArgumentException ex) {
            p.sendMessage(ChatColor.RED + ex.getMessage());
        }
        ENTRYPOINT.openInventory(p, "main");
    }

    @Override
    public void setMenuItems() {
        Set<String> kingdoms = KingdomsManager.getKingdomIDs();
        int i = 0;
        for (String kingdom : kingdoms) {
            String player = KingdomsManager.getKingdom(kingdom).getPlayer();
            inventory.setItem(i, makeHeadItem(Bukkit.getOfflinePlayer(player), kingdom, player));
            i++;
        }
    }
}
