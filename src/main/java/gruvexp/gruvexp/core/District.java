package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class District {

    public final String id;
    private Kingdom kingdom;

    private Material icon;

    private final HashMap<String, Section> sections = new HashMap<>();
    private final HashMap<String, Locality> localities = new HashMap<>();

    public District(String id, Kingdom kingdom, Material icon) {
        this.id = id;
        this.icon = icon;
        this.kingdom = kingdom;
    }

    @JsonCreator
    private District(String id, Material icon) {
        this.id = id;
        this.icon = icon;
    }

    public Kingdom getKingdom() {
        return kingdom;
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
        if (localities.containsKey(localityID)) return Component.text("Locality \"" + id + "\" already exists!", NamedTextColor.RED);

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

    public Collection<Locality> getLocalities() {
        return localities.values();
    }

    @JsonIgnore
    public Set<String> getLocalityIDs() {
        return localities.keySet();
    }

    public Component removeLocality(String localityID) {
        if (!localities.containsKey(localityID)) return Component.text("No locality with id \"" + localityID + "\" exists", NamedTextColor.RED);
        localities.remove(localityID);
        return Component.text("Successfully removed locality: ").append(Component.text(localityID));
    }

    public Component addSection(String sectionID, Coord entry) {
        if (sections.containsKey(sectionID)) return Component.text("Section \"" + sectionID + "\" already exists!", NamedTextColor.RED);
        sections.put(sectionID, new Section(sectionID, this, entry));
        return Component.text("Successfully added new rail section called ", NamedTextColor.GREEN).append(Component.text(sectionID, NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(" that starts at ")).append(entry.name());
    }

    public Section getSection(String sectionID) {
        return sections.get(sectionID);
    }

    @JsonIgnore
    public Collection<Section> getSections() {
        return sections.values();
    }

    @JsonIgnore
    public Set<String> getSectionIDs() {return sections.keySet();}

    public Component removeSection(String sectionID) {
        if (!sections.containsKey(sectionID)) return Component.text("No section with id \"" + sectionID + "\" exists", NamedTextColor.RED);
        sections.remove(sectionID);
        return Component.text("Successfully removed rail section: ").append(Component.text(sectionID));
    }

    public Component name() {
        return Component.text(id, NamedTextColor.GOLD);
    }

    public String tag() {
        return kingdom.id + ":" + id;
    }

    public Component address() {
        return kingdom.name().append(Component.text(":")).append(Component.text(id));
    }

    private boolean resolved = false;

    public void resolveReferences(Kingdom parentKingdom) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");
        resolved = true;
        this.kingdom = parentKingdom;
        getLocalities().forEach(locality -> locality.resolveReferences(this));
        getSections().forEach(section -> section.resolveReferences(this));
    }

    @JsonProperty("sections") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<Section> getSectionsJSON() {
        if (sections.isEmpty()) return null;
        return sections.values();
    }

    @JsonProperty("sections")
    private void setSectionsJSON(Collection<Section> sections) {
        sections.forEach(section -> this.sections.put(section.id, section));
    }

    @JsonProperty("localities") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<Locality> getLocalitiesJSON() {
        if (localities.isEmpty())  return null;
        return localities.values();
    }

    @JsonProperty("localities")
    private void setLocalitiesJSON(Collection<Locality> localities) {
        localities.forEach(locality -> this.localities.put(locality.id, locality));
    }
}
