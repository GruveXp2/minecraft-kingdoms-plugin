package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Villager;

import java.util.*;

public class Kingdom {

    public static final TextColor LABEL_COLOR = TextColor.color(0xff6633);
    public static final TextColor VALUE_COLOR = TextColor.color(0xff9f80);

    public final String id;

    private UUID kingID;
    private TextColor color = VALUE_COLOR;
    private final boolean isMale;

    private District postOfficeDistrict;
    private final HashMap<String, Citizen> citizens = new HashMap<>(); // holder alle villagers i kingdomet. key=navnet
    private final HashMap<String, District> districts = new HashMap<>();

    public Kingdom(@JsonProperty("id") String id,
                   @JsonProperty("king") UUID kingID,
                   @JsonProperty("isMale") boolean isMale) { // gender is binary to align with common sense and reality
        this.id = id;
        this.kingID = kingID;
        this.isMale = isMale;
    }

    @JsonIgnore
    public UUID getKingID() {
        return kingID;
    }

    public Component setKingID(UUID kingID) {
        this.kingID = kingID;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully set king of ").append(name())
                .append(Component.text(" to ")).append(king());
    }

    public Component setColor(TextColor color) {
        this.color = color;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully set color of ").append(name())
                .append(Component.text(" to ")).append(Component.text(color.asHexString(), color));
    }

    @JsonIgnore
    public District getPostOfficeDistrict() {
        return postOfficeDistrict;
    }

    public Component setPostOfficeDistrict(District district) {
        postOfficeDistrict = district;
        return  Component.text("Successfully set ").append(Component.text("post office district", District.LABEL_COLOR))
                .append(Component.text("of ")).append(name())
                .append(Component.text(" to ")).append(district.name());
    }

    public Component addDistrict(String districtID, Material icon) {
        if (districts.containsKey(districtID)) return Component.text("District \"" + id + "\" already exists!", NamedTextColor.RED);

        districts.put(districtID, new District(districtID, this, icon));

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully added ").append(Component.text("new district ", District.LABEL_COLOR))
                .append(Component.text(districtID, District.VALUE_COLOR))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(name())
                .append(Component.text(" with icon "))
                .append(Component.text(icon.name().toLowerCase()).color(NamedTextColor.BLUE));
    }

    public District getDistrict(String districtID) {
        return districts.get(districtID);
    }

    @JsonIgnore
    public Set<String> getDistrictIDs() {
        return districts.keySet();
    }

    @JsonIgnore
    public Collection<District> getDistricts() {
        return districts.values();
    }

    public Component removeDistrict(String districtID) {
        if (!districts.containsKey(districtID)) return Component.text("No distric twith id \"" + districtID + "\" exists", NamedTextColor.RED);
        districts.remove(districtID);

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully removed ").append(Component.text("district ", District.LABEL_COLOR)).append(Component.text(districtID));
    }

    public Component addCitizen(String name, Villager.Type variant, Villager.Profession profession) {
        if (citizens.containsKey(name)) return Component.text("A villager with that name already exists!", NamedTextColor.RED);
        citizens.put(name, new Citizen(name, this, variant, profession));

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully added new citizen called ", NamedTextColor.GREEN).append(Component.text(name));
    }

    public Citizen getCitizen(String name) {
        return citizens.get(name);
    }

    @JsonIgnore
    public Collection<Citizen> getCitizens() {
        return citizens.values();
    }

    public Component removeCitizen(String name) {
        if (!citizens.containsKey(name)) return Component.text("No citizen with name \"" + name + "\" exitst", NamedTextColor.RED);
        citizens.remove(name);

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully removed citizen called ").append(Component.text(name));
    }

    @JsonIgnore
    public Set<String> getCitizenNames() {
        return citizens.keySet();
    }

    public Component name() {
        return Component.text(Character.toUpperCase(id.charAt(0)) + id.substring(1), VALUE_COLOR)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                        ClickEvent.Payload.string("/kingdom " + id + " info")));
    }

    public Component king() {
        String playerName = Bukkit.getOfflinePlayer(kingID).getName();
        if (playerName == null) playerName = "failed to load name of ruler";
        return Component.text(isMale ? "King " : "Queen ").append(Component.text(playerName, color));
    }

    private boolean resolved = false;
    private String postOfficeDistrictIdDeferred;

    public void resolveReferences() {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");
        resolved = true;
        postOfficeDistrict = getDistrict(postOfficeDistrictIdDeferred);
        postOfficeDistrictIdDeferred = null;
        getDistricts().forEach(district -> district.resolveReferences(this));
        getCitizens().forEach(citizen -> citizen.resolveReferences(this));
    }

    @JsonProperty("isMale")
    private boolean getGenderJSON() {
        return isMale;
    }

    @JsonProperty("king")
    private String getKingJSON() {
        return kingID.toString();
    }

    @JsonProperty("color") @JsonInclude(JsonInclude.Include.NON_NULL)
    private void setColorJSON(String color) {
        this.color = TextColor.fromHexString(color);
    }

    @JsonProperty("color") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getColorJSON() {return color.asHexString();}

    @JsonProperty("postOfficeDistrict") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getPostOfficeDistrictJSON() {
        if (postOfficeDistrict == null) return null;
        return postOfficeDistrict.id;
    }

    @JsonProperty("postOfficeDistrict")
    private void setPostOfficeDistrictJSON(String postOfficeDistrict) {
        postOfficeDistrictIdDeferred = postOfficeDistrict;
    }

    @JsonProperty("districts") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private Collection<District> getDistrictsJSON() {
        if (districts.isEmpty()) return null;
        return districts.values();
    }

    @JsonProperty("districts")
    private void setDistrictsJSON(@JsonProperty("citizens") Collection<District> districts) {
        districts.forEach(district -> this.districts.put(district.id, district));
    }

    @JsonProperty("citizens") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<Citizen> getCitizensJSON() {
        if (citizens.isEmpty()) return null;
        return citizens.values();
    }

    @JsonProperty("citizens")
    private void setCitizensJSON(@JsonProperty("citizens") Collection<Citizen> citizens) {
        citizens.forEach(citizen -> this.citizens.put(citizen.name, citizen));
    }
}
