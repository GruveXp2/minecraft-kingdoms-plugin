package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CartManager {

    private static final HashMap<UUID, BukkitTask> CARTS = new HashMap<>();
    private static final HashMap<UUID, String[]> ADDRESSES = new HashMap<>();

    public static void registerCart(UUID cart, String[] address) {
        ADDRESSES.put(cart, address);
    }

    public static boolean isCartRegistered(UUID cart) {
        return ADDRESSES.containsKey(cart);
    }

    public static String[] getFullAddress(UUID cart) {
        return ADDRESSES.get(cart);
    }

    public static void driveCart(UUID cartUUID, Entity passenger, String startKingdom, String startDistrict, String section, char dir, String targetKingdom, String targetDistrict, String targetAddress) {

        CARTS.put(cartUUID, new DriveCart((Minecart) Objects.requireNonNull(Bukkit.getEntity(cartUUID)), passenger, startKingdom, startDistrict, section, dir, targetKingdom, targetDistrict, targetAddress).runTaskTimer(Main.getPlugin(), 0, 1));
    }

    public static void removeCart(UUID uuid) {
        CARTS.remove(uuid);
    }
    // add følgende: loadData() som laster data fra en fil som har lagra alle minecarts uuid og rail progress og section. hvis det ikke er lange til neste kryss, så pass på at man fortsatt kan skifte retninger osv.
    // saveData() lagrer alle minecarts som kjører på togbanen (ikke de som står stille på stasjonene) sine uuider, section og progress
    // når en vogn kommer inn i en section, så ser man på tiden til neste section, og tar runTaskLater og plugger inn "section_length" som ventetid. hvis sectionen har sånn at man kan velge, så add en til task som plasserer items i inventory, litt før den forrige tasken
}
