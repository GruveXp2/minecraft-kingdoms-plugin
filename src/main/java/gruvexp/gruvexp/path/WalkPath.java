package gruvexp.gruvexp.path;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.rail.DriveCart;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class WalkPath extends BukkitRunnable {

    int length;
    int totalDistance = 0;
    int currentIndex = 0;
    char direction; // hvilken retning carten kjører. når man kommer til svinger vil den nye retninga avhengig av den forrige retninga
    final Villager VILLAGER;
    Path path;
    final Location loc;
    final Location groundLoc;
    final Locality targetLocality;
    final Locality currentLocality;
    final String targetHouseNr;

    Vector ΔPos = new Vector(); // villageren skal ikke gå for fort, bare 4m/s = 0.2m/t.
    int timeUntilCalc = 0;

    public WalkPath(@NotNull Villager villager, @NotNull Locality currentLocality, @NotNull Locality targetLocality, String targetHouseNr, @NotNull Path path) {
        this.path = path;
        this.currentLocality = currentLocality;
        this.targetLocality = targetLocality;
        this.targetHouseNr = targetHouseNr;
        this.direction = path.getTurn(0); // <== når man lager paths så er den første tingen startretning
        this.loc = villager.getLocation();
        groundLoc = loc.clone().subtract(0, 0.4, 0);
        VILLAGER = villager;
        VILLAGER.setAI(false);
    }

    public void shutdown(Component error) {
        Component message = Component.text("A villager got stuck while walking! He probably has dementia and needs your guidance. Location:", NamedTextColor.RED)
                        .append(Utils.location(loc)).appendNewline()
                        .append(error);
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(message));
        cancel(); // i fremtiden så kommer det rød blocc og tekst som sier hva som gikk galt og at man kan ta /fix for å fikse det eller noe
        //tp villager til huset sitt
    }

    @Override
    public void run() {
        if (timeUntilCalc == 0) {
            calc();
        }
        //beveg villager med ΔPos
        loc.add(toVector(direction));
        groundLoc.add(toVector(direction));
        loc.setYaw(toYaw(direction));
        VILLAGER.teleport(loc); // I framtida! Add sånn at villidgeren ser i den retninga han går! GJØR SÅNN AT HAN KAN GÅ OPP HEIGHT BLOCCS SOM SLABS ETC.
        if (!loc.getBlock().isPassable()) { // går oppover hvis det er en blocc der, eks en slab, hvis det er en slab der så blir villidgeren telportert opp på slabben så gåanimasjonen ser ekte ut
            loc.add(0, 0.25, 0);
            groundLoc.add(0, 0.25, 0);
        } else if (groundLoc.getBlock().isPassable()) {
            loc.subtract(0, 0.2, 0);
            groundLoc.subtract(0, 0.2, 0);
        }
        timeUntilCalc--;
    }

    void calc() { // kalkulerer åssen retning man skal bevege seg i og om man skal endre path etc.
        timeUntilCalc = 5;
        if (path.hasBranch(currentIndex)) {
            if (currentLocality.getDistrict() != targetLocality.getDistrict()) { // hvis man skal til et annet kingdom/distrikt, så går man til stasjonen
                enterPath("station", null, Component.text("Couldent find route to district").append(targetLocality.getDistrict().name())
                        .append(Component.text(" or 'station'")));
            } else if (currentLocality != targetLocality) { // hvis man skal til en annen adresse så kan man komme der uten å ta toget (vil tippe dette er casen med Colin). men da må jeg adde border
                enterPath(targetLocality.id, "station", Component.text("Couldent find route to address ").append(targetLocality.name())
                        .append(Component.text(" or 'station'")));
            } else {
                enterPath(targetHouseNr, "*", Component.text("Couldent find route to " + targetLocality.id + ":" + targetHouseNr));
            }
        } else if (!path.hasBranches()) {
            shutdown(Component.text("Path").append(path.name()).append(Component.text("has no exit points!")));
        }
        if (path.getTurn(currentIndex) != null) {
            direction = path.getTurn(currentIndex);
        }
        currentIndex++;
        //teleportToGround();
    }

    void enterPath(String address, String fallBackAddress, Component errorMessage) { // Bytter over på den nye pathen dersom den har riktig adresse
        PathBranch branch = path.getBranch(currentIndex);
        if (branch.addresses().contains(address)) {
            Path path = branch.path();
            if (path == null) {
                // enterRail() // funksjon som gjør at villageren setter seg i en cart og kjører avgårde
                if (branch.addresses().contains("enter_rail")) enterRail();
                return;
            }
            int enterIndex = branch.enterIndex();
            if (!path.hasBranches()) {
                shutdown(Component.text("Path").append(path.name()).append(Component.text("has no exit points!")));
            }
            this.path = path;
            currentIndex = enterIndex; // hvis startindex er definert så bynner man midt i den neste pathen (etter startIndex blokker)
        } else if (fallBackAddress != null) { // hvis man ikke fant adressen sin og det var et endpoint, så er man støkk
            enterPath(fallBackAddress, null, errorMessage);
        } else if (path.isEndpoint(currentIndex)) { // hvis man ikke fant adressen sin og det var et endpoint, så er man støkk
            shutdown(errorMessage);
        }
    }

    void enterRail() { // setter seg oppi en minecart på adressens togstasjon og kjører mot målet (new )
        new DriveCart(VILLAGER, currentLocality.getEntrypoint(), targetLocality).runTaskTimer(Main.getPlugin(), 0, 1); // kanskje putt i cartmanager??
        VILLAGER.setAI(true);
        cancel(); // den slutter å gå på pathen
    }

    Vector toVector(char direction) {
        return switch (direction) {
            case 'n' -> new Vector(0   , 0, -0.2);
            case 'N' -> new Vector(0.2 , 0, -0.2);
            case 'e' -> new Vector(0.2 , 0, 0);
            case 'E' -> new Vector(0.2 , 0, 0.2);
            case 's' -> new Vector(0   , 0, 0.2);
            case 'S' -> new Vector(-0.2, 0, 0.2);
            case 'w' -> new Vector(-0.2, 0, 0);
            case 'W' -> new Vector(-0.2, 0, -0.2);
            default -> throw new IllegalStateException("Unexpected direction value: " + direction);
        };
    }

    int toYaw(char direction) {
        return switch (direction) {
            case 'n' -> -180;
            case 'N' -> -135;
            case 'e' -> -90;
            case 'E' ->-45;
            case 's' -> 0;
            case 'S' -> 45;
            case 'w' -> 90;
            case 'W' -> 135;
            default -> throw new IllegalStateException("Unexpected direction value: " + direction);
        };
    }

    /*public void teleportToGround() {
        if (!loc.getBlock().isPassable()) { // går oppover hvis det er en blocc der, eks en slab, hvis det er en slab der så blir villidgeren telportert opp på slabben så gåanimasjonen ser ekte ut
            loc.add(0, 0.5, 0);
        }
        Material blockBelow = loc.clone().getBlock().getType();

        if (blockBelow == Material.AIR) {
            // Villager is in the air, teleport it down
            while (blockBelow.getType() == Material.AIR) {
                loc = loc.subtract(0, -0.5, 0);
                blockBelow = loc.subtract(0, 1, 0).getBlock();
            }
        }
    }*/
}
