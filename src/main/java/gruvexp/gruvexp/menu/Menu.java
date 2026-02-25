package gruvexp.gruvexp.menu;

import gruvexp.gruvexp.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Menu implements InventoryHolder {

    protected final Inventory inventory;
    protected final ItemStack FILLER_GLASS = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ");
    protected final ItemStack LEFT = makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, Component.text("Prev page", NamedTextColor.AQUA));
    protected final ItemStack CLOSE = makeItem(Material.BARRIER, Component.text("Close menu", NamedTextColor.RED));
    protected final ItemStack RIGHT = makeItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, Component.text("Next page", NamedTextColor.AQUA));

    //The owner of the inventory created is the Menu itself,
    // so we are able to reverse engineer the Menu object from the
    // inventoryHolder in the MenuListener class when handling clicks
    public Menu() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());
        //grab all the items specified to be used for this menu and add to inventory
        this.setMenuItems();
    }

    // navnet på menyen
    public abstract Component getMenuName();

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
    public @NotNull Inventory getInventory() {
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
    public static ItemStack makeItem(Material material, Component displayName, List<Component> lore) {

        ItemStack item = new ItemStack(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(displayName);
        itemMeta.lore(lore);
        item.setItemMeta(itemMeta);

        return item;
    }

    public static ItemStack makeItem(Material material, Component displayName, Component... lore) {
        return makeItem(material, displayName, Arrays.asList(lore));
    }

    public static ItemStack makeItem(Material material, String displayName, String... lore) {
        return makeItem(material, Component.text(displayName), Arrays.stream(lore).map(Component::text).collect(Collectors.toList()));
    }

    public ItemStack makeHeadItem(OfflinePlayer p, String displayName, String... lore) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta itemMeta = (SkullMeta) item.getItemMeta();

        itemMeta.displayName(Component.text(displayName));
        itemMeta.lore(Arrays.stream(lore).map(Component::text).collect(Collectors.toList()));
        itemMeta.getPersistentDataContainer().set(
                new NamespacedKey(Main.getPlugin(), "uuid"),
                PersistentDataType.STRING,
                p.getUniqueId().toString()
        );
        itemMeta.setOwningPlayer(p);

        item.setItemMeta(itemMeta);
        return item;
    }
}
