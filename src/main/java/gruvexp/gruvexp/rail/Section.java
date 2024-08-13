package gruvexp.gruvexp.rail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.*;

public final class Section { // en delstrekning på en vei, GJØR SÅNN AT ENDPOINTROUTES KAN HA BARE 1 RUTE OG AT MAN IKKE TRENGER Å HA PILER FOR Å VELGE

    private Coord entry;
    private Coord exit;
    private int length; // hvor mange blokker man må kjøre før man kommer til et kryss
    private int speed = 1; // 1: normal, 2: fast, 3: express
    private String monoroute = ""; // "": kommer opp piler og man kan velge hvor man skal kjøre. en string: man kommer inn på en spesifik section i slutten uansett hva man gjør
    private String borderKingdom; // ender kingdom umiddelbart dersom det er spesifisert (dvs non null)
    private String borderDistrict; // samme som over bare med distrikt istedet
    private final HashMap<String, String> endPointShape = new HashMap<>(); // right: north_south
    private final HashMap<String, String> endPointSection = new HashMap<>(); // right: section_id
    private final HashMap<String, HashSet<String>> endPointAddresses = new HashMap<>(); // right: [pyralix, east_district, kano_bay]

    public void setEntry(Coord entry) {
        this.entry = entry;
    }

    public Coord getEntry() {return entry;}

    public void setExit(Coord exit) {
        this.exit = exit;
    }

    public Coord getExit() {return exit;}

    public void setRoute(String direction, String railShape, String nextSection, HashSet<String> addresses) {
        endPointShape.put(direction, railShape);
        endPointSection.put(direction, nextSection);
        endPointAddresses.put(direction, addresses);
        // Gjør sånn at den thrower exceptions hvis man skriver inn feil
    }

    @SuppressWarnings("unused") @JsonProperty("routes")// Blir brukt av JSONParseren
    private void setRoutes(Map<String, Map<String, Object>> routes) {
        for (Map.Entry<String, Map<String, Object>> entry : routes.entrySet()) {
            String direction = entry.getKey();
            Map<String, Object> directionData = entry.getValue();
            String shape = (String) directionData.get("shape");
            String sectionID = (String) directionData.get("section");
            HashSet<String> addresses = new HashSet<>((ArrayList<String>) directionData.get("addresses"));
            endPointShape.put(direction, shape);
            endPointSection.put(direction, sectionID);
            endPointAddresses.put(direction, addresses);
        }
    }

    @SuppressWarnings("unused") @JsonProperty("routes") @JsonInclude(JsonInclude.Include.NON_NULL) // Blir brukt av JSONParseren
    private Map<String, Map<String, Object>> getRoutes() {
        if (endPointShape.isEmpty()) {
            return null;
        }
        Map<String, Map<String, Object>> routes = new HashMap<>();
        for (String direction : endPointShape.keySet()) {
            Map<String, Object> route = new HashMap<>();
            route.put("shape", endPointShape.get(direction));
            route.put("section", endPointSection.get(direction));
            route.put("addresses", endPointAddresses.get(direction));
            routes.put(direction, route);
        }
        return routes;
    }

    public void removeRoute(String direction) {
        endPointShape.remove(direction);
    }

    public void removeAllRoutes() {
        endPointShape.clear();
        endPointSection.clear();
        endPointAddresses.clear();
    }

    public String getShape(String direction) {
        return endPointShape.get(direction);
    }

    public boolean hasRoutes() {
        return endPointShape.size() > 0;
    }

    public boolean hasRoute(String route) {
        return endPointShape.containsKey(route);
    }

    public void setMonoroute(String sectionId) { // sets a route at the end position. will disable endPointRoutes and it will just switch to that route when it gets there no matther what, and the player cant choose
        monoroute = sectionId;
    }

    public boolean hasMonoRoute() {
        return !Objects.equals(monoroute, "");
    }

    @SuppressWarnings("unused") @JsonProperty("monoroute") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getMonoroute() {
        if (Objects.equals(monoroute, "")) {
            return null;
        }
        return monoroute;
    }

    public void setBorder(String kingdom, String district) {
        borderKingdom = kingdom;
        borderDistrict = district;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBorderKingdom() {return borderKingdom;}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBorderDistrict() {return borderDistrict;}

    public void removeBorder() {
        borderKingdom = null;
        borderDistrict = null;
    }

    public boolean hasBorder() {
        return borderDistrict != null;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getSpeed() {
        return speed;
    }
    public void setLength(int length) {this.length = length;}

    public int getLength() {return length;}

    public String[] getEndpointData(String address) { // [dir, section]
        if (!Objects.equals(monoroute, "")) { // hvis monorute ikke er "" (dvs at seksjonen har en monorute)
            return new String[]{null, monoroute};
        }
        String dir;
        for (Map.Entry<String, HashSet<String>> entry : endPointAddresses.entrySet()) {
            if (entry.getValue().contains(address)) {
                dir = entry.getKey();
                return new String[]{dir, endPointSection.get(dir)};
            }
        }
        return null; // hvis minecarten ikke fant noen addresse å ta, og det ikke er monorute (da stopper carten opp og det kommer rød blocc over og står error)
    }
    public String
    print(String property) {
        switch (property) {
            case "entry" -> {
                if (entry == null) {
                    throw new IllegalArgumentException(ChatColor.RED + "This section doesnt have a entry point yet!");
                }
                return ChatColor.AQUA + "" + entry.getX() + " " + entry.getY() + " " + entry.getZ();
            } case "exit" -> {
                if (exit == null) {
                    throw new IllegalArgumentException(ChatColor.RED + "This section doesnt have a exit point yet!");
                }
                return ChatColor.AQUA + "" + exit.getX() + ", " + exit.getY() + ", " + exit.getZ();
            } case "length" -> {
                if (length == 0) {
                    throw new IllegalArgumentException(ChatColor.RED + "The length of this section is not calculated yet!");
                }
                return ChatColor.AQUA + "" + length + "m";
            } case "monoroute" -> {
                if (monoroute == null) {
                    throw new IllegalArgumentException(ChatColor.RED + "This section doesnt have a monoroute!");
                }
                return ChatColor.LIGHT_PURPLE + monoroute;
            } case "border" -> {
                if (borderDistrict == null) {
                    return ChatColor.YELLOW + "This section has no borders";
                }
                if (borderKingdom == null) {
                    return ChatColor.GOLD + borderDistrict;
                }
                return ChatColor.GOLD + borderKingdom + " : " + borderDistrict;
            } case "speed" -> { // maybe use int instead
                switch (speed) {
                    case 1:
                        return ChatColor.GREEN + "normal";
                    case 2:
                        return ChatColor.GREEN + "fast";
                    case 3:
                        return ChatColor.LIGHT_PURPLE + "express";
                }
            }
            default -> throw new IllegalArgumentException(ChatColor.RED + property + " is not a property!");
        }
        return null;
    }

    public String printRoutes() {
        StringBuilder out = new StringBuilder(ChatColor.BOLD + "Section has the following routes:\n");
        for (String dir : endPointShape.keySet()) {
            out.append(ChatColor.RED).append(dir).append(ChatColor.WHITE).append(":\n - Enters section ").append(ChatColor.LIGHT_PURPLE)
                    .append(endPointSection.get(dir)).append(ChatColor.WHITE).append("\n - Changes rail shape to ")
                    .append(ChatColor.GREEN).append(endPointShape.get(dir)).append("\n").append(ChatColor.WHITE).append(" - Adresses: ");
            for (String address : endPointAddresses.get(dir)) {
                out.append(ChatColor.GOLD).append(address).append(ChatColor.WHITE).append(", ");
            }
            out.delete(out.length() - 2, out.length());
            out.append("\n");
        }
        return out.toString();
    }

    @Override @Deprecated
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (entry != null) {
            out.append("\nentry ").append(entry.getX()).append(",").append(entry.getY()).append(",").append(entry.getZ());
        }
        if (exit != null) {
            out.append("\nexit ").append(exit.getX()).append(",").append(exit.getY()).append(",").append(exit.getZ());
        }
        if (hasRoutes()) {
            out.append("\nroutes");
            for (String dir : endPointShape.keySet()) {
                StringBuilder route = new StringBuilder(" ");
                route.append(dir).append(";").append(endPointShape.get(dir)).append(";").append(endPointSection.get(dir)).append(";");
                for (String address : endPointAddresses.get(dir)) {
                    route.append(address).append(",");
                }
                route.delete(route.length() - 1, route.length()); // sletter ekstra komma på slutten
                out.append(route);
            }
        }
        if (hasMonoRoute()) {
            out.append("\nmonoroute ").append(monoroute);
        }
        if (borderKingdom != null && borderDistrict != null) {
            out.append("\nborder ").append(borderKingdom).append(" ").append(borderDistrict);
        }
        out.append("\nspeed ").append(speed);
        if (length != 0) {
            out.append("\nlength ").append(length);
        }
        return out.toString();
    }

    @JsonIgnore
    public List<String> getProperties() { // brukes i tabcompletion kåmplisjen
        List<String> out = new ArrayList<>(6);
        if (entry != null) {
            out.add("entry");
        }
        if (exit != null) {
            out.add("exit");
        }
        if (hasRoutes()) {
            out.add("routes");
        }
        if (hasMonoRoute()) {
            out.add("monoroute");
        }
        if (borderDistrict != null) {
            out.add("border");
        }
        if (length > 0) {
            out.add("length");
        }
        out.add("speed");
        return out;
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

    private void summonBlockMarker(Location loc, Material block) {
        World world = Main.WORLD;
        float size = 0.5f;
        BlockDisplay display = (BlockDisplay) world.spawnEntity(new Location(world, loc.getX(), loc.getY(), loc.getZ()), EntityType.BLOCK_DISPLAY);
        display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(size, size, size), new AxisAngle4f(0, 0, 0, 1)));
        display.addScoreboardTag("rail_section_debug");
    }

    private void summonTextMarker(Location loc, String text) {
        World world = Main.WORLD;
        float size = 0.5f;
        TextDisplay display = (TextDisplay) world.spawnEntity(new Location(world, loc.getX(), loc.getY(), loc.getZ()), EntityType.TEXT_DISPLAY);
        display.setTransformation(new Transformation(new Vector3f(0, 0, 0), new AxisAngle4f(0, 0, 0, 1), new Vector3f(size, size, size), new AxisAngle4f(0, 0, 0, 1)));
        display.addScoreboardTag("rail_section_debug");
    }
}
