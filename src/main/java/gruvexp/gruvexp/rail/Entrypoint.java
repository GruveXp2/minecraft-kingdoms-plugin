package gruvexp.gruvexp.rail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.menu.menus.SelectAddressMenu;
import gruvexp.gruvexp.menu.menus.SelectDistrictMenu;
import gruvexp.gruvexp.menu.menus.SelectKingdomMenu;
import gruvexp.gruvexp.menu.menus.StationMenu;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * This class stores data related to train stations in the rail network.
 * It also stores menus that are used to select the address to go to.
 * When a player enters a minecart, its uuid will be checked in {@link KingdomsManager}, returning an id which can access the correct instance of this class.
 * The minecart will then grab the target address from the instance and start driving to the target address
 * @author  GruveXp
 */

public class Entrypoint {
    private Coord coord;
    private final String sectionID;
    private final String kingdomID;
    private final String districtID;
    private final String address;
    private String targetKingdom; // disse variablene brukes til å sette adresse til minecarten som de går oppi
    private String targetDistrict;
    private String targetAddress;
    private final StationMenu stationMenu;
    private SelectKingdomMenu selectKingdomMenu;
    private SelectDistrictMenu selectDistrictMenu;
    private SelectAddressMenu selectAddressMenu;
    private final char direction;
    private UUID cartUUID; // carten som står på stasjonen

    public Entrypoint(@JsonProperty("kingdom") String kingdomID, @JsonProperty("district") String districtID, @JsonProperty("address") String address, @JsonProperty("section") String sectionID, @JsonProperty("direction") char dir) {
        this.kingdomID = kingdomID;
        this.districtID = districtID;
        this.address = address;
        this.sectionID = sectionID;
        direction = dir;
        stationMenu = new StationMenu(this);
        try {
            init();
        } catch (NullPointerException e) {
            KingdomsManager.scheduleEntrypointInit(this); // sånn at coord og menus kan bli inita seinere etter at kingdoms hashmappet er ferig bygd
        }
    }

    public void init() {
        selectKingdomMenu = new SelectKingdomMenu(this);
        coord = KingdomsManager.getKingdom(kingdomID).getDistrict(districtID).getSection(sectionID).getEntry();
    }

    @JsonIgnore
    public Coord getCoord() {
        return coord;
    }

    @JsonProperty("section")
    public String getSectionID() {
        return sectionID;
    }

    @JsonProperty("kingdom")
    public String getKingdomID() {
        return kingdomID;
    }

    @JsonProperty("district")
    public String getDistrictID() {
        return districtID;
    }

    public String getAddress() {
        return address;
    }

    @JsonIgnore
    public String[] getFullAddress() {
        return new String[]{kingdomID, districtID, address};
    }

    public char getDirection() {
        return direction;
    }

    @JsonIgnore
    public String getTargetKingdom() {
        return targetKingdom;
    }

    @JsonIgnore
    public String getTargetDistrict() {
        return targetDistrict;
    }

    @JsonIgnore
    public String getTargetAddress() {
        return targetAddress;
    }

    public void setCartUUID(UUID cartUUID) {
        this.cartUUID = cartUUID;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public UUID getCartUUID() {
        return cartUUID;
    }

    public void openInventory(Player p, String menu) {
        switch (menu) {
            case "main" -> stationMenu.open(p);
            case "kingdom" -> selectKingdomMenu.open(p);
            case "district" -> selectDistrictMenu.open(p);
            case "address" -> selectAddressMenu.open(p);
        }
    }

    public void resetAddress() {
        targetKingdom = null;
        targetDistrict = null;
        targetAddress = null;
        selectDistrictMenu = null;
        selectAddressMenu = null;
    }

    public void setTargetKingdom(String kingdom) {
        targetKingdom = kingdom;
        stationMenu.setKingdom(kingdom);
        if (KingdomsManager.getKingdom(kingdom).getDistrictIDs().size() == 1) {
            setTargetDistrict(KingdomsManager.getKingdom(kingdom).getDistrictIDs().iterator().next());
        } else {
            targetDistrict = null;
            targetAddress = null;
        }
        selectDistrictMenu = new SelectDistrictMenu(this);
    }

    public void setTargetDistrict(String district) {
        targetDistrict = district;
        stationMenu.setDistrict(district);
        if (KingdomsManager.getKingdom(targetKingdom).getDistrict(district).getAddressIDs().size() == 1) {
            setTargetAddress(KingdomsManager.getKingdom(targetKingdom).getDistrict(district).getAddressIDs().iterator().next());
        } else {
            targetAddress = null;
        }
        selectAddressMenu = new SelectAddressMenu(this);
    }

    public void setTargetAddress(String address) {
        targetAddress = address;
        stationMenu.setAddress(address);
    }

    @JsonIgnore
    public StationMenu getStationMenu() {
        return stationMenu;
    }

    @Deprecated
    public String toString() {
        return address + " " + sectionID + " " + direction + " ";
    }
}
