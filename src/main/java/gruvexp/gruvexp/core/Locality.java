package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import gruvexp.gruvexp.rail.Entrypoint;
import gruvexp.gruvexp.rail.Section;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Locality {

    public final String id;
    private District district;

    private Material icon;
    private Entrypoint entrypoint;
    private HashMap<String, Path> paths = new HashMap<>();
    private HashMap<Integer, House> houses = new HashMap<>();

    public Locality(String id, District district, Material icon) {
        this.id = id;
        this.district = district;
        this.icon = icon;
    }
    public Locality(String id, @JsonProperty("material") Material icon) {
        this.id = id;
        this.icon = icon;
    }

    public void postInit() {
        for (Map.Entry<String, Path> pathEntry: paths.entrySet()) {
            pathEntry.getValue().initID(pathEntry.getKey());
        }
        if (!houses.isEmpty()) {
            for (House house : houses.values()) {
                house.postInit(this);
            }
        }
    }

    public District getDistrict() {
        return district;
    }

    public void setDistrict(District district) {
        if (this.district != null) throw new IllegalStateException("This address already have a district assigned to it!");
        this.district = district;
    }

    public Material getIcon() {
        return icon;
    }

    public Component setIcon(Material icon) {
        this.icon = icon;
        return Component.text("Successfully set icon of locality ").append(name())
                .append(Component.text(" to " + icon.toString()));
    }

    public Entrypoint getEntrypoint() {
        return entrypoint;
    }

    public Component setEntrypoint(Section section, char direction) {
        entrypoint = new Entrypoint(this, section, direction);
        return Component.text("Successfully set entrypoint in ", NamedTextColor.GREEN).append(name())
                .append(Component.text(" entering rail section ")).append(section.name())
                .append(Component.text(" in direction ")).append(Component.text(direction));
    }

    public Component addPath(String pathID, Coord startPos) {
        if (paths.containsKey(pathID)) return Component.text("Section \"" + pathID + "\" already exists!", NamedTextColor.RED);
        paths.put(pathID, new Path(pathID, startPos));
        return Component.text("Successfully added new path section called ", NamedTextColor.GREEN).append(Component.text(pathID, NamedTextColor.YELLOW))
                .append(Component.text(" that starts at ")).append(startPos.name());
    }

    public Path getPath(String pathID) {
        return paths.get(pathID);
    }

    @JsonIgnore
    public Set<String> getPathIDs() {
        return paths.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("paths") @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<String, Path> getPaths() {
        if (paths.isEmpty()) {return null;}
        return paths;
    }

    @SuppressWarnings("unused")
    private void setPaths(@JsonProperty("paths") HashMap<String, Path> paths) {
        this.paths = paths;
        KingdomsManager.scheduleAddressInit(this);
    }

    public Component removePath(String pathID) {
        if (!paths.containsKey(pathID)) return Component.text("No path section with id \"" + pathID + "\" exists", NamedTextColor.RED);
        paths.remove(pathID);
        return Component.text("Successfully removed path section: ").append(Component.text(pathID));
    }

    public Component addHouse(int houseNumber) {
        houses.put(houseNumber, new House(houseNumber, this));
        return Component.text("Successfully added a new house with house number ")
                .append(Component.text(houseNumber, NamedTextColor.BLUE))
                .append(Component.text(" in locality ")).append(address());
    }

    public House getHouse(int houseNumber) {
        House house = houses.get(houseNumber);
        if (house == null) {
            throw new IllegalArgumentException(ChatColor.RED + "House number \"" + houseNumber + "\" doesnt exist!");
        }
        return house;
    }

    public Component removeHouse(int houseNumber) {
        if (!houses.containsKey(houseNumber)) return Component.text("No house with house number \"" + houseNumber + "\" exists", NamedTextColor.RED);
        houses.remove(houseNumber);
        return Component.text("Successfully removed house: ")
                .append(name()).appendSpace().append(Component.text(houseNumber));
    }

    @JsonIgnore
    public Set<Integer> getHouseIDs() {
        return houses.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("houses") @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<Integer, House> getHouses() {
        if (houses.isEmpty()) {return null;}
        return houses;
    }

    @SuppressWarnings("unused")
    private void setHouses(@JsonProperty("houses") HashMap<Integer, House> houses) {
        this.houses = houses;
        KingdomsManager.scheduleAddressInit(this);
    }

    public Component name() {
        return Component.text(id, NamedTextColor.GOLD);
    }
    public String tag() {
        return district.tag() + ":" + id;
    }
    public Component address() {
        return district.address().append(Component.text(":")).append(Component.text(id));
    }
}
