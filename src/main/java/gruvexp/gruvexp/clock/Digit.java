package gruvexp.gruvexp.clock;

import gruvexp.gruvexp.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class Digit {

    private static ArrayList<ArrayList<Integer>> digitPlacements = new ArrayList<>(13);
    private static ArrayList<ArrayList<Integer>> digitToggles = new ArrayList<>(13);

    ArrayList<ArmorStand> armorStands = new ArrayList<>(13);
    private int digit = -1;
    private final double z;

    public static void init() {
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 7, 8, 9, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 3, 5, 8, 10)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 4, 5, 6, 7, 8, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 3, 5, 6, 7, 8, 9, 10, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 5, 6, 7, 9, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 3, 5, 8, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12)));
        digitPlacements.add(new ArrayList<>(Arrays.asList(0, 1, 2, 3, 5, 6, 7, 8, 9, 10, 11, 12)));

        digitToggles.add(new ArrayList<>(Arrays.asList(6, 4)));
        digitToggles.add(new ArrayList<>(Arrays.asList(1, 2, 4, 7, 9, 11, 12)));
        digitToggles.add(new ArrayList<>(Arrays.asList(1, 2, 3, 4, 6, 7, 11, 12)));
        digitToggles.add(new ArrayList<>(Arrays.asList(3, 4)));
        digitToggles.add(new ArrayList<>(Arrays.asList(1, 2, 9, 11)));
        digitToggles.add(new ArrayList<>(Arrays.asList(1, 2, 8, 11)));
        digitToggles.add(new ArrayList<>(Collections.singletonList(4)));
        digitToggles.add(new ArrayList<>(Arrays.asList(1, 2, 4, 6, 7, 8, 9)));
        digitToggles.add(new ArrayList<>(Arrays.asList(1, 2, 4, 6, 7, 9)));
        digitToggles.add(new ArrayList<>(Collections.singletonList(4)));
    }

    public Digit(double x, double y, double z) {
        this.z = z;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 3; j++) {
                if (j == 1 && (i == 1 || i == 3)) {continue;} // Hull i 8 tallet
                ArmorStand e = (ArmorStand) Main.WORLD.spawnEntity(new Location(Main.WORLD, x + j*0.625, y + i*0.625, z + 0.5625), EntityType.ARMOR_STAND);
                e.getEquipment().setHelmet(new ItemStack(Material.LIGHT_BLUE_CONCRETE));
                e.setInvisible(true);
                e.setGravity(false);
                armorStands.add(e);
            }
        }
    }

    public int getDigit() {
        return digit;
    }

    public void setDigit(int digit) {
        if (digit == this.digit) {return;}
        if (digit > 9) {
            throw new java.lang.IllegalArgumentException("Digit " + digit + " is too large, must be between 0 and 9");
        }
        this.digit = digit;
        for (int i = 0; i < 13; i++) { // flytter armor stands til riktig plass
            if (digitPlacements.get(digit).contains(i)) { // flytt blokk ut
                Location loc = armorStands.get(i).getLocation();
                loc.setZ(z);
                armorStands.get(i).teleport(loc);
            } else { // flytt blokk inn
                Location loc = armorStands.get(i).getLocation();
                loc.setZ(z + 0.5625);
                armorStands.get(i).teleport(loc);
            }
        }
    }

    public void increaseDigit() {
        // init en liste der det står hvilke som blir toggla on og off
        digit ++;
        if (digit == 10) {digit = 0;}
        for (int i : digitToggles.get(digit)) {
            Location loc = armorStands.get(i).getLocation();
            if (loc.getZ() == z) {
                loc.setZ(z + 0.5625);
            } else {
                loc.setZ(z);
            }
            armorStands.get(i).teleport(loc);
        }
    }

    public void delete() {
        for (ArmorStand as : armorStands) {
            as.remove();
        }
    }

}
