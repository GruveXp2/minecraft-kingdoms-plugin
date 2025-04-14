package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class Locality {

    public static final TextColor LABEL_COLOR = TextColor.color(0xffdd33);
    public static final TextColor VALUE_COLOR = TextColor.color(0xfff080);

    public final String id;
    private District district;

    private Material icon;

    private Entrypoint entrypoint;
    private final HashMap<String, Path> paths = new HashMap<>();
    private final HashMap<Integer, House> houses = new HashMap<>();

    public Locality(String id, District district, Material icon) {
        this.id = id;
        this.district = district;
        this.icon = icon;
    }

    @JsonCreator
    private Locality(@JsonProperty("id") String id,
                     @JsonProperty("icon") Material icon,
                     @JsonProperty("entrypoint") Entrypoint entrypoint) {
        this.id = id;
        this.icon = icon;
        this.entrypoint = entrypoint;
    }

    @JsonIgnore
    public District getDistrict() {
        return district;
    }

    public Material getIcon() {
        return icon;
    }

    public Component setIcon(Material icon) {
        this.icon = icon;

        KingdomsManager.save = true;
        return Component.text("Successfully set icon of ").append(Component.text("locality ", LABEL_COLOR)).append(name())
                .append(Component.text(" to " + icon.toString()));
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Entrypoint getEntrypoint() {
        return entrypoint;
    }

    public Component setEntrypoint(Section section, char direction) {
        entrypoint = new Entrypoint(this, section, direction);

        KingdomsManager.save = true;
        return Component.text("Successfully set entrypoint in ").append(name())
                .append(Component.text(" entering ")).append(Component.text("rail section ", Section.LABEL_COLOR)).append(section.name())
                .append(Component.text(" in direction ")).append(Component.text(direction));
    }

    public Component addPath(String pathID, Coord startPos) {
        if (paths.containsKey(pathID)) return Component.text("Section \"" + pathID + "\" already exists!", NamedTextColor.RED);
        paths.put(pathID, new Path(pathID, this, startPos));

        KingdomsManager.save = true;
        return Component.text("Successfully added ").append(Component.text("new path section ", Path.LABEL_COLOR)).append(Component.text(pathID, Path.VALUE_COLOR))
                .append(Component.text(" that starts at ")).append(startPos.name());
    }

    public Path getPath(String pathID) {
        return paths.get(pathID);
    }

    public Collection<Path> getPaths() {
        return paths.values();
    }

    @JsonIgnore
    public Set<String> getPathIDs() {
        return paths.keySet();
    }

    public Component removePath(String pathID) {
        if (!paths.containsKey(pathID)) return Component.text("No path section with id \"" + pathID + "\" exists", NamedTextColor.RED);
        paths.remove(pathID);

        KingdomsManager.save = true;
        return Component.text("Successfully removed ").append(Component.text("path section", Path.LABEL_COLOR)).append(Component.text(": "))
                .append(Component.text(pathID, Path.VALUE_COLOR));
    }

    public Component addHouse(int houseNumber) {
        houses.put(houseNumber, new House(houseNumber, this));

        KingdomsManager.save = true;
        return Component.text("Successfully added a new house with house number ")
                .append(Component.text(houseNumber, NamedTextColor.BLUE))
                .append(Component.text(" in locality ")).append(address());
    }

    public House getHouse(int houseNumber) {
        return houses.get(houseNumber);
    }

    public Collection<House> getHouses() {
        return houses.values();
    }

    @JsonIgnore
    public Set<Integer> getHouseIDs() {
        return houses.keySet();
    }

    public Component removeHouse(int houseNumber) {
        if (!houses.containsKey(houseNumber)) return Component.text("No house with house number \"" + houseNumber + "\" exists", NamedTextColor.RED);
        houses.remove(houseNumber);

        KingdomsManager.save = true;
        return Component.text("Successfully removed house: ")
                .append(name()).appendSpace().append(Component.text(houseNumber));
    }

    public Component name() {
        return Component.text(id, VALUE_COLOR);
    }
    public String tag() {
        return district.tag() + ":" + id;
    }
    public Component address() {
        return district.address().append(Component.text(":")).append(Component.text(id, VALUE_COLOR));
    }

    private boolean resolved = false;

    public void resolveReferences(District parentDistrict) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");
        resolved = true;
        this.district = parentDistrict;
        if (entrypoint != null) {
            entrypoint.resolveReferences(this);
        }
        getPaths().forEach(path -> path.resolveReferences(this));
        getHouses().forEach(house -> house.resolveReferences(this));
    }

    @JsonProperty("houses") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<House> getHousesJSON() {
        if (houses.isEmpty()) return null;
        return houses.values();
    }

    @JsonProperty("houses")
    private void setHousesJSON(@JsonProperty("houses") Collection<House> houses) {
        houses.forEach(house -> this.houses.put(house.nr, house));
    }

    @JsonProperty("paths") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Collection<Path> getPathsJSON() {
        if (paths.isEmpty()) return null;
        return paths.values();
    }

    @JsonProperty("paths")
    private void setPathsJSON(@JsonProperty("paths") Collection<Path> paths) {
        paths.forEach(path -> this.paths.put(path.id, path));
    }
}
