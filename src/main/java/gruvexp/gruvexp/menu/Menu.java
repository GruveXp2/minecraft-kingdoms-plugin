package gruvexp.gruvexp.menu;

import gruvexp.gruvexp.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;

public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;
    protected final ItemStack FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    protected final ItemStack LEFT = makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ChatColor.AQUA + "Prev page");
    protected final ItemStack CLOSE = makeItem(Material.BARRIER, ChatColor.RED + "Close menu");
    protected final ItemStack RIGHT = makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, ChatColor.AQUA + "Next page");

    //The owner of the inventory created is the Menu itself,
    // so we are able to reverse engineer the Menu object from the
    // inventoryHolder in the MenuListener class when handling clicks
    public Menu() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        //grab all the items specified to be used for this menu and add to inventory
        this.setMenuItems();
    }

    // navnet på menyen
    public abstract String getMenuName();

    // hvor mange slots
    public abstract int getSlots();

    // kode som kjøres når man trykker på et item
    public abstract void handleMenu(InventoryClickEvent e);

    // hvilke items som er inni
    public abstract void setMenuItems();

    // åpner inventory for en player
    public void open(Player p) {
        p.openInventory(inventory);
    }

    //Overridden method from the InventoryHolder interface
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    // fyller tomme slots med glass
    public void setFillerGlass(){
        for (int i = 0; i < getSlots(); i++) {
            if (inventory.getItem(i) == null){
                inventory.setItem(i, FILLER_GLASS);
            }
        }
    }

    // lager et item
    public static ItemStack makeItem(Material material, String displayName, String... lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.setLore(Arrays.asList(lore));
        item.setItemMeta(itemMeta);

        return item;
    }

    public ItemStack makeHeadItem(OfflinePlayer p, String displayName, String... lore) { // i fremtiden gjør sånn at fargen på itemet er kingdommens farge
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();
        itemMeta.setDisplayName(displayName);
        itemMeta.getPersistentDataContainer().set(new NamespacedKey(Main.getPlugin(), "uuid"), PersistentDataType.STRING, p.getUniqueId().toString());
        itemMeta.setLore(Arrays.asList(lore));
        itemMeta.setOwningPlayer(p);

        item.setItemMeta(itemMeta);
        return item;
    }
}
