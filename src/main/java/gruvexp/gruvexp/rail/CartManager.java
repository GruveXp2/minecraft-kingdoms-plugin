package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.Locality;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class CartManager {

    private static final HashMap<UUID, BukkitTask> carts = new HashMap<>();
    private static final HashMap<UUID, Locality> localities = new HashMap<>();

    public static void registerCart(UUID cart, Locality locality) {
        localities.put(cart, locality);
    }

    public static boolean isCartRegistered(UUID cart) {
        return localities.containsKey(cart);
    }

    public static Locality getLocality(UUID cart) {
        return localities.get(cart);
    }

    public static void driveCart(UUID cartUUID, Entity passenger, Section startSection, char dir, Locality targetLocality) {

        carts.put(cartUUID, new DriveCart((Minecart) Objects.requireNonNull(Bukkit.getEntity(cartUUID)), passenger, startSection, dir, targetLocality).runTaskTimer(Main.getPlugin(), 0, 1));
    }

    public static void removeCart(UUID uuid) {
        carts.remove(uuid);
    }
    // add følgende: loadData() som laster data fra en fil som har lagra alle minecarts uuid og rail progress og section. hvis det ikke er lange til neste kryss, så pass på at man fortsatt kan skifte retninger osv.
    // saveData() lagrer alle minecarts som kjører på togbanen (ikke de som står stille på stasjonene) sine uuider, section og progress
    // når en vogn kommer inn i en section, så ser man på tiden til neste section, og tar runTaskLater og plugger inn "section_length" som ventetid. hvis sectionen har sånn at man kan velge, så add en til task som plasserer items i inventory, litt før den forrige tasken
}
