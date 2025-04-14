package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class District {

    public static final TextColor LABEL_COLOR = TextColor.color(0xff9933);
    public static final TextColor VALUE_COLOR = TextColor.color(0xffbf80);

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
    private District(@JsonProperty("id") String id,
                     @JsonProperty("icon") Material icon) {
        this.id = id;
        this.icon = icon;
    }

    @JsonIgnore
    public Kingdom getKingdom() {
        return kingdom;
    }

    public Material getIcon() {
        return icon;
    }

    public Component setIcon(Material icon) {
        this.icon = icon;

        KingdomsManager.save = true;
        return Component.text("Successfully set icon of ").append(Component.text("district ", District.LABEL_COLOR)).append(name())
                .append(Component.text(" to ")).append(Component.text(icon.toString().toLowerCase(), NamedTextColor.GREEN));
    }

    public Component addLocality(String localityID, Material material) {
        if (localities.containsKey(localityID)) return Component.text("Locality \"" + id + "\" already exists!", NamedTextColor.RED);

        localities.put(localityID, new Locality(localityID, this, material));

        KingdomsManager.save = true;
        return Component.text("Successfully added ").append(Component.text("new locality ", Locality.LABEL_COLOR))
                .append(Component.text(localityID, Locality.VALUE_COLOR))
                .append(Component.text(" to "))
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

        KingdomsManager.save = true;
        return Component.text("Successfully removed ").append(Component.text("locality", Locality.LABEL_COLOR)).append(Component.text(": "))
                .append(Component.text(localityID, Locality.VALUE_COLOR));
    }

    public Component addSection(String sectionID, Coord entry) {
        if (sections.containsKey(sectionID)) return Component.text("Section \"" + sectionID + "\" already exists!", NamedTextColor.RED);
        sections.put(sectionID, new Section(sectionID, this, entry));

        KingdomsManager.save = true;
        return Component.text("Successfully added ").append(Component.text("new rail section ", Section.LABEL_COLOR)).append(Component.text(sectionID, Section.VALUE_COLOR))
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

        KingdomsManager.save = true;
        return Component.text("Successfully removed ").append(Component.text("rail section", Section.LABEL_COLOR)).append(Component.text(": "))
                .append(Component.text(sectionID, Section.VALUE_COLOR));
    }

    public Component name() {
        return Component.text(id, VALUE_COLOR)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/district " + id + " info"));
    }

    public String tag() {
        return kingdom.id + ":" + id;
    }

    public Component address() {
        return kingdom.name().append(Component.text(":")).append(Component.text(id, VALUE_COLOR));
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
