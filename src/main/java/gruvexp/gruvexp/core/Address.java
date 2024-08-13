package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.path.Path;
import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Address {

    private final Material MATERIAL;
    private HashMap<String, Path> paths = new HashMap<>();
    private HashMap<Integer, House> houses = new HashMap<>();

    public Address(@JsonProperty("material") Material material) {
        MATERIAL = material;
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

    public Material getMaterial() {
        return MATERIAL;
    }

    public void addPath(String pathID, Path path) {
        if (paths.containsKey(pathID)) {
            throw new IllegalArgumentException(ChatColor.RED + "Path \"" + pathID + "\" already exist!");
        }
        paths.put(pathID, path);
    }

    public Path getPath(String pathID) {
        Path path = paths.get(pathID);
        if (path == null) {
            throw new IllegalArgumentException(ChatColor.RED + "Path \"" + pathID + "\" doesnt exist!");
        }
        return path;
    }

    @JsonIgnore
    public Set<String> getPathIDs() {
        return paths.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("paths") @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<String, Path> getPaths() {
        if (paths.size() == 0) {return null;}
        return paths;
    }

    @SuppressWarnings("unused")
    private void setPaths(@JsonProperty("paths") HashMap<String, Path> paths) {
        this.paths = paths;
        KingdomsManager.scheduleAddressInit(this);
    }

    public void removePath(String pathID) {
        paths.remove(pathID);
    }

    public void addHouse(int houseNumber) {
        houses.put(houseNumber, new House());
    }

    public House getHouse(int houseNumber) {
        House house = houses.get(houseNumber);
        if (house == null) {
            throw new IllegalArgumentException(ChatColor.RED + "House number \"" + houseNumber + "\" doesnt exist!");
        }
        return house;
    }

    public House getHouse(String houseNumber) {
        int houseNumberInt;
        try {
            houseNumberInt = Integer.parseInt(houseNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(String.format("%sHouse number must be a number!\n%sExpected number, got %s\"%s\"%s instead", ChatColor.RED, ChatColor.WHITE, ChatColor.RED, houseNumber, ChatColor.WHITE));
        }
        return getHouse(houseNumberInt);
    }

    @JsonIgnore
    public Set<Integer> getHouseIDs() {
        return houses.keySet();
    }

    @SuppressWarnings("unused") @JsonProperty("houses") @JsonInclude(JsonInclude.Include.NON_NULL)
    private HashMap<Integer, House> getHouses() {
        if (houses.size() == 0) {return null;}
        return houses;
    }

    @SuppressWarnings("unused")
    private void setHouses(@JsonProperty("houses") HashMap<Integer, House> houses) {
        this.houses = houses;
        KingdomsManager.scheduleAddressInit(this);
    }

}
