package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.rail.Section;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Set;

public class District {

    public final String id;
    private Kingdom kingdom;

    private final Material icon;
    private final HashMap<String, Address> addresses = new HashMap<>();
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
        addresses.put(id, new Address(id, this, material));
    }

    public Address getAddress(String addressID) {
        Address address = addresses.get(addressID);
        if (address == null) {
            throw new IllegalArgumentException(ChatColor.RED + "Address \"" + addressID + "\" doesnt exist!");
        }
        return address;
    }

    public boolean hasAddress(String addressID) {
        return addresses.containsKey(addressID);
    }

    @JsonIgnore
    public Set<String> getAddressIDs() {
        return addresses.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("addresses") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private HashMap<String, Address> getAddresses() {
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
}
