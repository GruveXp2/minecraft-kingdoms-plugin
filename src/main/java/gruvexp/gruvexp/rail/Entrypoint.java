package gruvexp.gruvexp.rail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.menu.menus.SelectLocalityMenu;
import gruvexp.gruvexp.menu.menus.SelectDistrictMenu;
import gruvexp.gruvexp.menu.menus.SelectKingdomMenu;
import gruvexp.gruvexp.menu.menus.StationMenu;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
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
    private Locality locality;

    public final char direction;
    private Section section;
    private Kingdom targetKingdom; // disse variablene brukes til å sette adresse til minecarten som de går oppi
    private District targetDistrict;
    private Locality targetLocality;
    private final StationMenu stationMenu;
    private SelectKingdomMenu selectKingdomMenu;
    private SelectDistrictMenu selectDistrictMenu;
    private SelectLocalityMenu selectLocalityMenu;
    private UUID cartUUID; // carten som står på stasjonen

    public Entrypoint(Locality locality, Section section, char direction) {
        this.locality = locality;
        this.section = section;
        this.direction = direction;
        stationMenu = new StationMenu(this);
    }

    @JsonCreator
    private Entrypoint(@JsonProperty("direction") char direction,
                       @JsonProperty("cartUUID") String cartUUID) {
        this.direction = direction;
        if (cartUUID != null) {
            this.cartUUID = UUID.fromString(cartUUID);
        }
        stationMenu = new StationMenu(this);
    }

    @JsonIgnore
    public Coord getCoord() {
        return section.getEntry();
    }

    @JsonIgnore
    public Locality getLocality() {
        return locality;
    }

    @JsonIgnore
    public Section getSection() {
        return section;
    }

    public char getDirection() {
        return direction;
    }

    @JsonIgnore
    public Kingdom getTargetKingdom() {
        return targetKingdom;
    }

    @JsonIgnore
    public District getTargetDistrict() {
        return targetDistrict;
    }

    @JsonIgnore
    public Locality getTargetLocality() {
        return targetLocality;
    }

    public void setCartUUID(UUID cartUUID) {
        this.cartUUID = cartUUID;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public UUID getCartUUID() {
        return cartUUID;
    }

    public void resetAddress() {
        targetKingdom = null;
        targetDistrict = null;
        targetLocality = null;
        selectDistrictMenu = null;
        selectLocalityMenu = null;
    }

    public void setTargetKingdom(Kingdom kingdom) {
        targetKingdom = kingdom;
        stationMenu.setKingdom(kingdom);
        if (kingdom.getDistrictIDs().size() == 1) {
            setTargetDistrict(kingdom.getDistricts().iterator().next());
        } else {
            targetDistrict = null;
            targetLocality = null;
        }
        selectDistrictMenu = new SelectDistrictMenu(this);
    }

    public void setTargetDistrict(District district) {
        targetDistrict = district;
        stationMenu.setDistrict(district);
        if (targetDistrict.getLocalityIDs().size() == 1) {
            setTargetLocality(targetDistrict.getLocalities().iterator().next());
        } else {
            targetLocality = null;
        }
        selectLocalityMenu = new SelectLocalityMenu(this);
    }

    public void setTargetLocality(Locality locality) {
        targetLocality = locality;
        stationMenu.setLocality(locality);
    }

    @JsonIgnore
    public StationMenu getStationMenu() {
        return stationMenu;
    }

    public void spawnCart(EntityType entityType) {
        Minecart newCart = (Minecart) Main.WORLD.spawnEntity(getCoord().toLocation(Main.WORLD), entityType);
        CartManager.registerCart(newCart.getUniqueId(), locality);
        newCart.addScoreboardTag(locality.getDistrict().getKingdom().id + "-" + locality.getDistrict().id + "-" + locality.id);
    }

    public void openInventory(Player p, String menu) {
        switch (menu) {
            case "main" -> stationMenu.open(p);
            case "kingdom" -> selectKingdomMenu.open(p);
            case "district" -> selectDistrictMenu.open(p);
            case "address" -> selectLocalityMenu.open(p);
        }
    }

    private boolean resolved = false;
    private String sectionDeferred;

    public void resolveReferences(Locality parentLocality) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");
        resolved = true;
        this.locality = parentLocality;
        if (sectionDeferred != null) {
            section = locality.getDistrict().getSection(sectionDeferred);
            sectionDeferred = null;
        }
        selectKingdomMenu = new SelectKingdomMenu(this);
    }

    @JsonProperty("section")
    private String getSectionJSON() {
        return section.id;
    }

    @JsonProperty("section")
    private void setSectionJSON(String section) {
        sectionDeferred = section;
    }
}
