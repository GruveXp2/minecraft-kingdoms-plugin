package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.rail.Entrypoint;
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

    private final Material icon;
    private final HashMap<String, Locality> addresses = new HashMap<>();
    private final HashMap<String, Entrypoint> entrypoints = new HashMap<>(); // key = address
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

    public void addAddress(String id, Material material) {
        if (addresses.containsKey(id)) {
            throw new IllegalArgumentException(ChatColor.RED + "Address \"" + id + "\" already exist!");
        }
        addresses.put(id, new Locality(id, this, material));
    }

    public Locality getLocality(String localityID) {
        return addresses.get(localityID);
    }

    public boolean hasAddress(String addressID) {
        return addresses.containsKey(addressID);
    }

    @JsonIgnore
    public Set<String> getLocalityIDs() {
        return addresses.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("addresses") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private HashMap<String, Locality> getAddresses() {
        if (addresses.isEmpty()) {
            return null;
        }
        return addresses;
    }

    public void addSection(String sectionID, Section section) {
        if (sections.containsKey(sectionID)) {
            throw new IllegalArgumentException(ChatColor.RED + "Section \"" + sectionID + "\" already exist!");
        }
        sections.put(sectionID, section);
    }

    public void removeSection(String sectionID) {
        sections.remove(sectionID);
    }

    public Section getSection(String sectionID) {
        Section section = sections.get(sectionID);
        if (section == null) {
            throw new IllegalArgumentException(ChatColor.RED + "Section \"" + sectionID + "\" doesnt exist!");
        }
        return section;
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

    public Material getMaterial() {
        return icon;
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
}
