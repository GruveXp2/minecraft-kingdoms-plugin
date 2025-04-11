package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Set;

public class District {

    public final String id;
    private Kingdom kingdom;

    private Material icon;
    private final HashMap<String, Locality> localities = new HashMap<>();
    private final HashMap<String, Section> sections = new HashMap<>();

    public District(String id, Kingdom kingdom, Material icon) {
        this.id = id;
        this.icon = icon;
        this.kingdom = kingdom;
    }

    public Kingdom getKingdom() {
        return kingdom;
    }

    public void setKingdom(Kingdom kingdom) {
        if (this.kingdom != null) throw new IllegalStateException("This district already have a district assigned to it!");
        this.kingdom = kingdom;
    }

    public Material getIcon() {
        return icon;
    }

    public Component setIcon(Material icon) {
        this.icon = icon;
        return Component.text("Successfully set icon of district ").append(name())
                .append(Component.text(" to " + icon.toString()));
    }

    public Component addLocality(String localityID, Material material) {
        if (localities.containsKey(localityID)) {
            throw new IllegalArgumentException(ChatColor.RED + "Address \"" + localityID + "\" already exist!");
        }
        localities.put(localityID, new Locality(localityID, this, material));
        return Component.text("Successfully added locality ")
                .append(Component.text(localityID, NamedTextColor.GOLD))
                .append(Component.text(" to ", NamedTextColor.GREEN))
                .append(name())
                .append(Component.text(" with icon "))
                .append(Component.text(icon.name().toLowerCase()).color(NamedTextColor.BLUE));
    }

    public Locality getLocality(String localityID) {
        return localities.get(localityID);
    }

    public Component removeLocality(String localityID) {
        if (!localities.containsKey(localityID)) return Component.text("No locality with id \"" + localityID + "\" exists", NamedTextColor.RED);
        localities.remove(localityID);
        return Component.text("Successfully removed locality: ").append(Component.text(localityID));
    }

    public boolean hasLocality(String localityID) {
        return localities.containsKey(localityID);
    }

    @JsonIgnore
    public Set<String> getLocalityIDs() {
        return localities.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("localities") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private HashMap<String, Locality> getLocalities() {
        if (localities.isEmpty()) {
            return null;
        }
        return localities;
    }

    public Component addSection(String sectionID, Coord entry) {
        if (sections.containsKey(sectionID)) return Component.text("Section \"" + sectionID + "\" already exists!", NamedTextColor.RED);
        sections.put(sectionID, new Section(sectionID, entry));
        return Component.text("Successfully added new rail section called ", NamedTextColor.GREEN).append(Component.text(sectionID, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" that starts at ")).append(entry.name());
    }

    public Section getSection(String sectionID) {
        return sections.get(sectionID);
    }

    public Component removeSection(String sectionID) {
        if (!sections.containsKey(sectionID)) return Component.text("No section with id \"" + sectionID + "\" exists", NamedTextColor.RED);
        sections.remove(sectionID);
        return Component.text("Successfully removed rail section: ").append(Component.text(sectionID));
    }

    @JsonIgnore
    public Set<String> getSectionIDs() {return sections.keySet();}

    @SuppressWarnings("unused") @JsonProperty("sections") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private HashMap<String, Section> getSections() {
        if (sections.isEmpty()) {
            return null;
        }
        return sections;
    }

    public boolean notContainsSection(String sectionID) {
        return !sections.containsKey(sectionID);
    }

    public void setEntrypoint(String kingdom, String district, String address, String section, char dir) {
        entrypoints.put(address, new Entrypoint(kingdom, district, address, section, dir));
    }

    public Entrypoint getEntrypoint(String address) {
        return entrypoints.get(address);
    }

    @SuppressWarnings("unused") @JsonProperty("entrypoints") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private HashMap<String, Entrypoint> getEntrypoints() {
        if (entrypoints.isEmpty()) {
            return null;
        }
        return entrypoints;
    }

    public void removeEntrypoint(String address) {
        entrypoints.remove(address);
    }

    public Component name() {
        return Component.text(id, NamedTextColor.GOLD);
    }

    public Component address() {
        return kingdom.name().append(Component.text(":")).append(Component.text(id));
    }
}
