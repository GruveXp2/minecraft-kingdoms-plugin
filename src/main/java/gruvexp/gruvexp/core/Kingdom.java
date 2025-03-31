package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Kingdom {

    public final String ID;
    private final HashMap<String, District> districts = new HashMap<>();
    private HashMap<String, Citizen> citizens = new HashMap<>(); // holder alle villagers i kingdomet. key=navnet
    private final UUID kingID;
    private final boolean isMale;
    private String postOfficeDistrict;

    public Kingdom(@JsonProperty("id") String kingdomID, @JsonProperty("player") UUID kingID, boolean isMale) { // gender is strictly binary to align with common sense and reality
        this.ID = kingdomID;
        this.kingID = kingID;
        this.isMale = isMale;
    }

    public void postInit() {
        for (Map.Entry<String, Citizen> citizenEntry : citizens.entrySet()) {
            citizenEntry.getValue().postInit(citizenEntry.getKey(), this);
        }
    }

    public UUID getKingID() {
        return kingID;
    }

    public void addDistrict(String districtID, District district) {
        if (districts.containsKey(districtID)) {
            throw new IllegalArgumentException(ChatColor.RED + "District \"" + districtID + "\" already exist!");
        }
        districts.put(districtID, district);
    }

    public District getDistrict(String districtID) {
        District district = districts.get(districtID);
        if (district == null) {
            throw new IllegalArgumentException(ChatColor.RED + "District \"" + districtID + "\" doesnt exist!");
        }
        return district;
    }

    @JsonIgnore
    public Set<String> getDistrictIDs() {
        return districts.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("districts") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private HashMap<String, District> getDistricts() {
        if (districts.isEmpty()) { // sånn at det ikke kommer med i josn hvis egenskapen ikke er der. TA PÅ DE ANDRE OGSÅ FOR Å FRIGJØRE PLASS!
            return null;
        }
        return districts;
    }

    @SuppressWarnings("unused")
    public void setPostOfficeDistrict(@JsonProperty("postOfficeDistrict") String district) {
        postOfficeDistrict = district;
    }

    public String getPostOfficeDistrict() {
        if (postOfficeDistrict == null) {
            throw new IllegalArgumentException(ChatColor.RED + "This kingdom hasnt registered a post office yet!");
        }
        return postOfficeDistrict;
    }

    @SuppressWarnings("unused") @JsonProperty("postOfficeDistrict") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getPostOfficeDistrictJSON() {
        return postOfficeDistrict;
    }

    public void addCitizen(String name, Citizen citizen) {
        if (citizens.containsKey(name)) {
            throw new IllegalArgumentException(ChatColor.GREEN + name + ChatColor.RED + " already exists!");
        }
        citizens.put(name, citizen);
    }

    public Citizen getCitizen(String name) {
        if (!citizens.containsKey(name)) {
            throw new IllegalArgumentException(ChatColor.RED + "Citizen \"" + name + "\" doesnt exist!");
        }
        return citizens.get(name);
    }

    @JsonIgnore
    public Set<String> getCitizenNames() {
        return citizens.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("citizens") @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<String, Citizen> getCitizens() {
        if (citizens.isEmpty()) {
            return null;
        }
        return citizens;
    }

    @SuppressWarnings("unused")
    private void setCitizens(@JsonProperty("citizens") HashMap<String, Citizen> citizens) {
        this.citizens = citizens;
        KingdomsManager.scheduleKingdomInit(this);
    }

    @Override
    public String toString() {
        return ID;
    }

    public TextComponent name() {
        return Component.text(Character.toUpperCase(ID.charAt(0)) + ID.substring(1), NamedTextColor.GOLD);
    }

    public TextComponent king() {
        return Component.text(isMale ? "King " : "Queen ").append(Bukkit.getPlayer(kingID).name());
    }
}
