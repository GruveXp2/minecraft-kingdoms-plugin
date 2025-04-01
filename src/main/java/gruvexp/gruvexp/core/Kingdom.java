package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Villager;

import java.util.*;

public class Kingdom {

    public final String id;
    private final HashMap<String, District> districts = new HashMap<>();
    private HashMap<String, Citizen> citizens = new HashMap<>(); // holder alle villagers i kingdomet. key=navnet
    private UUID kingID;
    private TextColor color;
    private final boolean isMale;
    private String postOfficeDistrict;

    public Kingdom(String id, @JsonProperty("player") UUID kingID, boolean isMale) { // gender is strictly binary to align with common sense and reality
        this.id = id;
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

    public Component setKingID(UUID kingID) {
        this.kingID = kingID;
        return Component.text("Successfully set king of ").append(name())
                .append(Component.text(" to ")).append(king());
    }

    public TextColor getColor() {return color;}

    public Component setColor(TextColor color) {
        this.color = color;
        return Component.text("Successfully set color of ").append(name())
                .append(Component.text(" to ")).append(Component.text(color.asHexString(), color));
    }

    public Component addDistrict(String districtID, Material icon) {
        if (districts.containsKey(districtID)) return Component.text("District \"" + id + "\" already exists!", NamedTextColor.RED);

        districts.put(districtID, new District(districtID, this, icon));
        return Component.text("Successfully added district ")
                .append(Component.text(districtID, NamedTextColor.GOLD))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(name())
                .append(Component.text(" with icon "))
                .append(Component.text(icon.name().toLowerCase()).color(NamedTextColor.BLUE));
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

    public Component removeDistrict(String districtID) {
        if (!districts.containsKey(districtID)) return Component.text("No distric twith id \"" + districtID + "\" exists", NamedTextColor.RED);
        districts.remove(districtID);
        return Component.text("Successfully removed district: ").append(Component.text(districtID));
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

    public Component addCitizen(String name, Villager.Type variant, Villager.Profession profession) {
        if (citizens.containsKey(name)) return Component.text("A villager with that name already exists!", NamedTextColor.RED);
        citizens.put(name, new Citizen(name, this, variant, profession));
        return Component.text("Successfully added new citizen called ", NamedTextColor.GREEN).append(Component.text(name));
    }

    public Citizen getCitizen(String name) {
        if (!citizens.containsKey(name)) {
            throw new IllegalArgumentException(ChatColor.RED + "Citizen \"" + name + "\" doesnt exist!");
        }
        return citizens.get(name);
    }

    public Component removeCitizen(String name) {
        if (!citizens.containsKey(name)) return Component.text("No citizen with name \"" + name + "\" exitst", NamedTextColor.RED);
        citizens.remove(name);
        return Component.text("Successfully removed citizen called ").append(Component.text(name));
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
        return id;
    }

    public Component name() {
        return Component.text(Character.toUpperCase(id.charAt(0)) + id.substring(1), NamedTextColor.GOLD);
    }

    public Component king() {
        return Component.text(isMale ? "King " : "Queen ").append(Bukkit.getPlayer(kingID).name());
    }
}
