package gruvexp.gruvexp.path;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.rail.DriveCart;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WalkPath extends BukkitRunnable {

    int length;
    int totalDistance = 0;
    int counter = 0;
    char direction; // hvilken retning carten kjører. når man kommer til svinger vil den nye retninga avhengig av den forrige retninga
    final Villager VILLAGER;
    Path path;
    Location loc;
    Location groundLoc;
    String targetKingdomID;
    String kingdomID;
    String targetDistrictID;
    String districtID;
    String targetAddressID;
    String addressID;
    Address address;
    String targetAddressNr;

    Vector dPos = new Vector(); // ΔPos, villageren skal ikke gå for fort, bare 4m/s = 0.2m/t.
    int timeUntilCalc = 0;

    public WalkPath(@NotNull Villager villager, @NotNull String kingdomID, @NotNull String districtID, @NotNull String addressID, @NotNull String targetKingdomID, @NotNull String targetDistrictID, @NotNull String targetAddressID, String targetAddressNr, @NotNull Path path) {
        this.path = path;
        this.kingdomID = kingdomID;
        this.districtID = districtID;
        this.addressID = addressID;
        this.targetKingdomID = targetKingdomID;
        this.targetDistrictID = targetDistrictID;
        this.targetAddressID = targetAddressID;
        this.targetAddressNr = targetAddressNr;
        this.direction = path.getTurn(0); // <== når man lager paths så er den første tingen startretning
        this.loc = villager.getLocation();
        groundLoc = loc.clone().subtract(0, 0.4, 0);
        VILLAGER = villager;
        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
        District district = kingdom.getDistrict(districtID);
        address = district.getAddress(addressID);
        VILLAGER.setAI(false);
    }

    public void shutdown(String error) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(String.format(ChatColor.RED + "A villager got stuck while walking! Location: %.2f %.0f %.2f\n%s", loc.getX(), loc.getY(), loc.getZ(), error)));
        cancel(); // i fremtiden så kommer det rød blocc og tekst som sier hva som gikk galt og at man kan ta /fix for å fikse det eller noe
        //tp villager til huset sitt
    }


    @Override
    public void run() {
        if (timeUntilCalc == 0) {
            calc();
        }
        //beveg villager med delta_pos (dPos)
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

    void calc() { // kalkulerer hvilken retning man skal bevege seg i og om man skal endre path etc.
        timeUntilCalc = 5;
        if (path.hasBranch(counter)) {
            if (!Objects.equals(kingdomID, targetKingdomID) || !Objects.equals(districtID, targetDistrictID)) { // hvis man skal til et annet kingdom/distrikt, så går man til stasjonen
                enterPath("station", null, String.format("Couldent find route to %s:%s or 'station'", targetKingdomID, targetDistrictID));
            } else if (!Objects.equals(addressID, targetAddressID)) { // hvis man skal til en annen adresse så kan man komme der uten å ta toget (vil tippe dette er casen med Colin). men da må jeg adde border
                enterPath(targetAddressID, "station", "Couldent find route to address " + ChatColor.GOLD + targetAddressID + " or 'station'");
            } else {
                enterPath(targetAddressNr, "*", "Couldent find route to " + ChatColor.GOLD + targetAddressNr + ". " + targetAddressID);
            }
        } else if (!path.hasBranches()) {
            shutdown("Path " + ChatColor.YELLOW + path + " has no exit points!");
        }
        if (path.getTurn(counter) != null) {
            direction = path.getTurn(counter);
        }
        counter++;
        //teleportToGround();
    }

    void enterPath(String addressID, String fallBackAddress, String errorMessage) { // Bytter over på den nye pathen dersom den har riktig adresse
        if (path.getBranchAddresses(counter).contains(addressID)) {
            String pathID = path.getBranchPathID(counter);
            if (pathID.equals("enter_rail")) {
                // enterRail() // funksjon som gjør at villageren setter seg i en cart og kjører avgårde
                enterRail();
                return;
            }
            int startIndex = path.getBranchEnterIndex(counter);
            path = address.getPath(pathID);
            if (!path.hasBranches()) {
                shutdown("Path " + ChatColor.YELLOW + path + " has no exit points!");
            }
            counter = startIndex; // hvis startindex er definert så bynner man midt i den neste pathen (etter startIndex blokker)
        } else if (fallBackAddress != null) { // hvis man ikke fant adressen sin og det var et endpoint, så er man støkk
            enterPath(fallBackAddress, null, errorMessage);
        } else if (path.isEndpoint(counter)) { // hvis man ikke fant adressen sin og det var et endpoint, så er man støkk
            shutdown(errorMessage);
        }
    }

    void enterRail() { // setter seg oppi en minecart på adressens togstasjon og kjører mot målet (new )
        new DriveCart(VILLAGER, KingdomsManager.getKingdom(kingdomID).getDistrict(districtID).getEntrypoint(addressID), targetKingdomID, targetDistrictID, targetAddressID).runTaskTimer(Main.getPlugin(), 0, 1); // kanskje putt i cartmanager??
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
