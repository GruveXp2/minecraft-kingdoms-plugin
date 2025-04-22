package gruvexp.gruvexp.rail;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public final class Section {

    public static final TextColor LABEL_COLOR = TextColor.color(0xff3399);
    public static final TextColor VALUE_COLOR = TextColor.color(0xff80bf);

    public final String id;
    private District district;

    private Coord entry;
    private Coord exit;
    private int length; // hvor mange blokker man må kjøre før man kommer til et kryss
    private final NavigableMap<Integer, Integer> speedChanges = new TreeMap<>();

    private Section nextSection; // når man kommer på slutten, så går man inn i denne seksjonen (overstyrer ruter)
    private District border; // man sjekker både kdom og distr når man går over grensa
    private final HashMap<String, RailRoute> routingTable = new HashMap<>(); // finn ut åssen json funker her. mna bare, tar og samler alle RailRoutes i et sett og lagrer sammen med hashset med string adresser

    public Section(String id, District district, Coord entry) {
        this.id = id;
        this.district = district;
        this.entry = entry;
    }

    @JsonCreator
    private Section(@JsonProperty("id") String id,
                    @JsonProperty("entry") Coord entry,
                    @JsonProperty("exit") Coord exit,
                    @JsonProperty("length") int length) {
        this.id = id;
        this.entry = entry;
        this.exit = exit;
        this.length = length;
    }

    @JsonIgnore
    public District getDistrict() {
        return district;
    }

    @NotNull
    public Coord getEntry() {
        if (entry == null) throw new IllegalStateException("Missing entry in section " + id);
        return entry;
    }

    public Component setEntry(Coord entry) {
        if (this.entry == entry) return Component.text("Nothing happened, that was already set as the entry", NamedTextColor.YELLOW);
        this.entry = entry;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully set entry of ").append(name())
                .append(Component.text(" to ")).append(entry.name());
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Coord getExit() {return exit;}

    public Component setExit(Coord exit) {
        if (this.exit == exit) return Component.text("Nothing happened, that was already set as the ecit", NamedTextColor.YELLOW);
        this.exit = exit;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully set exit of ").append(name())
                .append(Component.text(" to ")).append(exit.name());
    }

    public RailRoute getEndpointRoute(String address) {
        return routingTable.get(address);
    }

    public boolean hasRoutes() {
        return !routingTable.isEmpty();
    }

    public Component setRoute(String direction, Rail.Shape railShape, Section nextSection, HashSet<String> addresses) {
        // GJØR DIRECTION OM TIL ENUM!
        routingTable.entrySet().removeIf(entry -> direction.equals(entry.getValue().direction())); // removes any routes already using that direction

        RailRoute route = new RailRoute(nextSection, railShape, direction);
        for (String address : addresses) {
            routingTable.put(address, route); // it works kinda like a router in data communication
        }
        Component nextSectionWarning = hasNextSection() ?
                Component.text("\nWarning: this will switch to routing mode, and will overwrite the current set next section", NamedTextColor.YELLOW) : Component.empty();
        this.nextSection = null;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully added route:\n")
                .append(Component.text(direction, NamedTextColor.BLUE))
                .append(Component.text(" -> "))
                .append(route.targetSection().name()).append(Component.text(", shape: "))
                .append(Component.text(route.railShape().name().toLowerCase(), NamedTextColor.GREEN)).appendNewline()
                .append(Component.text(" - for addresses ").append(Component.text(String.join(", ", addresses), NamedTextColor.GOLD))).appendNewline()
                .append(nextSectionWarning);
    }

    public Component removeRoute(String direction) {
        routingTable.entrySet().removeIf(entry -> direction.equals(entry.getValue().direction()));

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully removed route for direction ").append(Component.text(direction, NamedTextColor.GREEN));
    }

    public Component removeAllRoutes() {
        if (routingTable.isEmpty()) return Component.text("Nothing happened, there was no routes to remove", NamedTextColor.YELLOW);
        routingTable.clear();
        KingdomsManager.registerEdit(this);
        return Component.text("Successfully removed all routes (this will switch to having a specific next section to go to, or being the last stop)");
    }

    @JsonIgnore
    public Section getNextSection() {
        return nextSection;
    }

    public boolean hasNextSection() {
        return nextSection != null;
    }

    public Component setNextSection(Section section) { // sets a route at the end position. will disable endPointRoutes, and it will just switch to that route when it gets there no matther what, and the player cant choose
        if (nextSection == section) return Component.text("Nothing happened, that section was already selected as the next rail section", NamedTextColor.YELLOW);
        nextSection = section;

        KingdomsManager.registerEdit(this);
        if (section == null) return Component.text("Successfully set ").append(name()).append(Component.text(" as an endpoint"));
        return Component.text("Successfully set ").append(Component.text("next section", Section.LABEL_COLOR)).append(Component.text(" (the one to link to) of ")).append(name())
                .append(Component.text(" to ")).append(section.name());
    }

    @JsonIgnore
    public District getBorder() {
        return border;
    }

    public boolean hasBorder() {
        return border != null;
    }

    public Component setBorder(District targetDistrict) {
        if (border == targetDistrict) return Component.text("Nothing happened, that was already the border", NamedTextColor.YELLOW);
        border = targetDistrict;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully set border of ").append(name())
                .append(Component.text(" to ")).append(targetDistrict.address());
    }

    public Component removeBorder() {
        if (border == null) return Component.text("Nothing happened, this section didnt have a border in the first place", NamedTextColor.YELLOW);
        border = null;

        KingdomsManager.registerEdit(this);
        return Component.text("Successfullt removed border of ").append(name());
    }

    public Integer getSpeed(int index) {
        return speedChanges.get(index);
    }

    public int getNextSpeedIndex(int index) { // neste speed eller den man er på nå
        if (speedChanges.isEmpty()) return Integer.MAX_VALUE;
        Integer next = speedChanges.higherKey(index - 1);
        return next != null ? next : Integer.MAX_VALUE;
    }

    @JsonIgnore
    public int getLength() {return length;}

    public Component calculateLength(String direction, Player p) {
        if (getExit() == null) return Component.text("Cant calculate length! Section must have an exit point", NamedTextColor.RED);
        new CalculateLength(this, direction.toCharArray()[0], p).runTaskTimer(Main.getPlugin(), 0, 1);
        return Component.text("Starting to calculate rail length of ").append(name()).append(Component.text(" ..."));
    }

    @JsonIgnore
    void setLength(int length) {
        if (length == this.length) return;
        this.length = length;
        KingdomsManager.registerEdit(this);
    }
    // ikke ferdig
    /*public void show() { // viser blockdisplays og tekst om seksjonen
        Location loc = entry.toLocation(Main.WORLD);
        summonBlockMarker(loc, Material.LIME_CONCRETE);
        summonBlockMarker(exit.toLocation(Main.WORLD), Material.YELLOW_CONCRETE);
        // looper gjennom seksjonen for å sette ned en blokk langs heile strekninga
        for (int i = 0; i < length; i++) {
            Material material = loc.getBlock().getType();
            if (material == Material.AIR) {
                Material material2 = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ()).getBlock().getType();
                if (material2 == Material.RAIL || material2 == Material.POWERED_RAIL) {
                    loc.add(0, -1, 0);
                } else {
                    summonBlockMarker(loc, Material.RED_CONCRETE);
                    return;
                }
            } else {
                if (material != Material.RAIL && material != Material.POWERED_RAIL) {
                    p.sendMessage(ChatColor.RED + "Cart derailed, length calculation cancelled");
                    cancel();
                    return;
                }
                Rail data = (Rail) loc.getBlock().getBlockData();
                Rail.Shape shape = data.getShape();
                switch (direction) {
                    case 'n' -> {
                        switch (shape) {
                            case SOUTH_EAST -> direction = 'e'; // 2 måter å svinge, ellers går den bare rett fram
                            case SOUTH_WEST -> direction = 'w';
                            case ASCENDING_NORTH -> loc.add(0, 1, 0); // hvis det er bakke så telporteres den opp/ned i tilegg
                            case ASCENDING_SOUTH -> loc.add(0, -1, 0);
                        }
                    }
                    case 's' -> {
                        switch (shape) {
                            case NORTH_EAST -> direction = 'e';
                            case NORTH_WEST -> direction = 'w';
                            case ASCENDING_NORTH -> loc.add(0, -1, 0);
                            case ASCENDING_SOUTH -> loc.add(0, 1, 0);
                        }
                    }
                    case 'e' -> {
                        switch (shape) {
                            case NORTH_WEST -> direction = 'n';
                            case SOUTH_WEST -> direction = 's';
                            case ASCENDING_EAST -> loc.add(0, 1, 0);
                            case ASCENDING_WEST -> loc.add(0, -1, 0);
                        }
                    }
                    case 'w' -> {
                        switch (shape) {
                            case NORTH_EAST -> direction = 'n';
                            case SOUTH_EAST -> direction = 's';
                            case ASCENDING_EAST -> loc.add(0, -1, 0);
                            case ASCENDING_WEST -> loc.add(0, 1, 0);
                        } // ellers er direction den samme
                    }
                }
            }
            switch (direction) { // endrer loc i den retninga som er "direction"
                case 'n' -> loc.add(0, 0, -1);
                case 's' -> loc.add(0, 0, 1);
                case 'e' -> loc.add(1, 0, 0);
                case 'w' -> loc.add(-1, 0, 0);
            }
            cart.teleport(loc);
            if (loc.equals(exit)) {
                district.getSection(sectionID).setLength(counter);
                p.sendMessage("100% - Rail length: " + ChatColor.AQUA + counter + "m");
                KingdomsManager.save = true;
                cart.remove();
                cancel();
            }
            doubleRun = !doubleRun;
            if (doubleRun) {
                run();
            }
        }
    }*/

    private void summonBlockMarker(@NotNull Location loc, Material block) {
        World world = Main.WORLD;
        float size = 0.5f;
        BlockDisplay display = (BlockDisplay) world.spawnEntity(new Location(world, loc.getX(), loc.getY(), loc.getZ()), EntityType.BLOCK_DISPLAY);
        display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(size, size, size), new AxisAngle4f(0, 0, 0, 1)));
        display.addScoreboardTag("rail_section_debug");
    }

    private void summonTextMarker(@NotNull Location loc, String text) {
        World world = Main.WORLD;
        float size = 0.5f;
        TextDisplay display = (TextDisplay) world.spawnEntity(new Location(world, loc.getX(), loc.getY(), loc.getZ()), EntityType.TEXT_DISPLAY);
        display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(size, size, size), new AxisAngle4f(0, 0, 0, 1)));
        display.addScoreboardTag("rail_section_debug");
    }

    public @NotNull Component name() {
        return Component.text(id, VALUE_COLOR)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/rail " + id + " info"));
    }

    public static Component speed(int speedValue) {
        return speedShort(speedValue).append(Component.text(" km/h"));
    }

    public static Component speedShort(int speedValue) {
        return switch (speedValue) {
            case 1 -> Component.text("40", NamedTextColor.WHITE);
            case 2 -> Component.text("70", NamedTextColor.YELLOW);
            case 3 -> Component.text("110", NamedTextColor.BLUE);
            case 4 -> Component.text("140", TextColor.color(0x8f57ff));
            default -> throw new IllegalStateException("Unexpected speed value: " + speedValue);
        };
    }

    public Component speeds() {
        Component out = Component.text("Speeds: ");
        for (var entry : speedChanges.entrySet()) {
            out = out.append(Component.text(entry.getKey())).append(Component.text("->")).append(speedShort(entry.getValue())).appendSpace();
        }
        return out;
    }

    public Component routes() {
        Component routeInfo;
        if (hasRoutes()) {
            routeInfo = Component.text("Routes:\n");
            Map<RailRoute, List<String>> reversed = new HashMap<>(); // its gonna map the routes to addresses instead so its easier to read when printed

            for (Map.Entry<String, RailRoute> entry : routingTable.entrySet()) {
                String address = entry.getKey();
                RailRoute route = entry.getValue();

                reversed.computeIfAbsent(route, r -> new ArrayList<>()).add(address);
            }

            for (Map.Entry<RailRoute, List<String>> entry : reversed.entrySet()) {
                String addresses = String.join(", ", entry.getValue());
                RailRoute route = entry.getKey();
                routeInfo = routeInfo.append(Component.text(route.direction(), NamedTextColor.BLUE))
                        .append(Component.text(" -> "))
                        .append(route.targetSection().name()).append(Component.text(", shape: "))
                        .append(Component.text(route.railShape().name().toLowerCase(), NamedTextColor.GREEN)).appendNewline()
                        .append(Component.text(" - for addresses: ").append(Component.text(addresses, NamedTextColor.GOLD))).appendNewline();
            }
        } else if (hasNextSection()) {
            routeInfo = Component.text("Route: enters ").append(Component.text("section ", LABEL_COLOR)).append(nextSection.name());
        } else {
            routeInfo = Component.text("This section doesnt lead to other sections (final stop)");
        }
        return routeInfo;
    }

    private HashMap<Location, Integer> speedPositions;

    public Component setSpeedPos(Player p, Coord coord, int speed) {
        if (speedPositions == null) speedPositions = new HashMap<>();

        Location loc = new Location(p.getWorld(), coord.x() + 0.5, coord.y(), coord.z() + 0.5);
        if (speedPositions.get(loc) != null && speedPositions.get(loc) == speed) return Component.text("Nothing happened, speed change already had that value at the given position", NamedTextColor.YELLOW);
        speedPositions.put(loc, speed);

        return Component.text("Successfully set speed changing to ").append(speed(speed))
                .append(Component.text(" at ")).append(coord.name())
                .append(Component.text(" for ")).append(name()).appendNewline()
                .append(Component.text("Remember to run /rail calculate when youre done setting speed changes to save the results", NamedTextColor.YELLOW));
    }

    HashMap<Location, Integer> getSpeedPositions() {
        return speedPositions;
    }

    @JsonIgnore
    public Set<Integer> getSpeedIndexes() {
        return speedChanges.keySet();
    }

    void setSpeed(int index, int speed) {
        speedChanges.put(index, speed);
        KingdomsManager.registerEdit(this);
    }

    public Component removeSpeed(int index) {
        if (!speedChanges.containsKey(index)) return Component.text("Nothing happened, no speed change was registered at that location", NamedTextColor.YELLOW);
        speedChanges.remove(index);

        KingdomsManager.registerEdit(this);
        return Component.text("Successfully removed speed change at index " + index);
    }

    private boolean resolved = false;
    private String nextSectionDeferred;
    private String borderDeferred;
    Map<String, Map<String, Object>> routesDeferred;

    public void resolveReferences(District parentDistrict) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");

        this.district = parentDistrict;
        if (borderDeferred != null) {
            String[] borderList = borderDeferred.split(":");
            Kingdom kingdom = KingdomsManager.getKingdom(borderList[0]);
            this.border = kingdom.getDistrict(borderList[1]);
            borderDeferred = null;
        }
        if (nextSectionDeferred != null) {
            nextSection = border != null ? border.getSection(nextSectionDeferred) : district.getSection(nextSectionDeferred);
            nextSectionDeferred = null;
        }
        if (routesDeferred != null) {
            for (Map.Entry<String, Map<String, Object>> entry : routesDeferred.entrySet()) {
                String direction = entry.getKey();
                Map<String, Object> routeData = entry.getValue();
                String shape = (String) routeData.get("shape");
                String sectionID = (String) routeData.get("section");
                HashSet<String> addresses = new HashSet<>((ArrayList<String>) routeData.get("addresses"));
                District routeDistrict = border != null ? border : district;
                RailRoute route = new RailRoute(routeDistrict.getSection(sectionID), Rail.Shape.valueOf(shape), direction);
                for (String address : addresses) {
                    routingTable.put(address, route);
                }
            }
            routesDeferred = null;
        }
        resolved = true;
    }

    @JsonProperty("length") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer getLengthJSON() {
        if (length == 0) return null;
        return length;
    }

    @JsonProperty("speeds") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Integer> getSpeedJSON() {
        if (speedChanges.isEmpty()) return null;
        return speedChanges;
    }

    @JsonProperty("speeds")
    private void setSpeedJSON(Map<Integer, Integer> speedChanges) {
        this.speedChanges.putAll(speedChanges);
    }

    @JsonProperty("nextSection") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getNextSectionJSON() {
        if (nextSection == null) return null;
        return nextSection.id;
    }

    @JsonProperty("nextSection")
    private void setNextSectionJSON(String nextSection) {
        if (Objects.equals(nextSection, "end")) return;
        nextSectionDeferred = nextSection;
    }

    @JsonProperty("border") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getBorderJSON() {
        if (border == null) return null;
        return border.tag();
    }

    @JsonProperty("border")
    private void setBorderJSON(String border) {
        borderDeferred = border;
    }

    @JsonProperty("routes") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Map<String, Object>> getRoutes() {
        if (routingTable.isEmpty()) return null;

        Map<RailRoute, List<String>> reversed = new HashMap<>();
        for (Map.Entry<String, RailRoute> entry : routingTable.entrySet()) {
            String address = entry.getKey();
            RailRoute route = entry.getValue();

            reversed.computeIfAbsent(route, r -> new ArrayList<>()).add(address);
        }
        Map<String, Map<String, Object>> routes = new HashMap<>();
        for (Map.Entry<RailRoute, List<String>> entry : reversed.entrySet()) {
            RailRoute routeData = entry.getKey();
            List<String> addresses = entry.getValue();
            Map<String, Object> route = new HashMap<>();
            route.put("shape", routeData.railShape());
            route.put("section", routeData.targetSection().id);
            route.put("addresses", addresses);
            routes.put(routeData.direction(), route);
        }
        return routes;
    }

    @JsonProperty("routes")
    private void setRoutes(Map<String, Map<String, Object>> routes) {
        routesDeferred = routes;
    }
}
