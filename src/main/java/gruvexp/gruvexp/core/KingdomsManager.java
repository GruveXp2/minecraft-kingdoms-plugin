package gruvexp.gruvexp.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import gruvexp.gruvexp.rail.Entrypoint;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.data.Rail;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class KingdomsManager {

    public static final ImmutableSet<String> RAIL_SHAPES = ImmutableSet.of("north_south", "east_west", "north_east", "north_west", "south_east", "south_west");
    public static final ImmutableSet<String> ROUTES = ImmutableSet.of("forward", "right", "left");
    public static final ImmutableSet<String> SIGNS = ImmutableSet.of("forward", "right", "left", "forward_roundabout", "right_roundabout", "left_roundabout", "u_turn");
    public static final ImmutableSet<String> DIRECTIONS = ImmutableSet.of("n", "s", "e", "w");
    private static HashMap<String, Kingdom> kingdoms;
    public static final HashSet<String> BLOCKS = new HashSet<>();
    public static boolean save = false;

    public static final ImmutableMap<String, Rail.Shape> string2Rail = ImmutableMap.of(
            "north_south", Rail.Shape.NORTH_SOUTH,
            "east_west", Rail.Shape.EAST_WEST,
            "north_east", Rail.Shape.NORTH_EAST,
            "north_west", Rail.Shape.NORTH_WEST,
            "south_east", Rail.Shape.SOUTH_EAST,
            "south_west", Rail.Shape.SOUTH_WEST);

    private static final HashSet<Entrypoint> entrypointPostInit = new HashSet<>();
    private static final HashSet<Address> addressPostInit = new HashSet<>();
    private static final HashSet<Kingdom> kingdomPostInit = new HashSet<>();
    private static final HashSet<Citizen> citizenPostInit = new HashSet<>();

    public static void init() {
        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                BLOCKS.add(material.toString().toLowerCase());
            }
        }
        for (Map.Entry<String, Kingdom> kingdomEntry : kingdoms.entrySet()) {
            kingdomEntry.getValue().initID(kingdomEntry.getKey());
        }
        for (Entrypoint entrypoint : entrypointPostInit) {
            entrypoint.init();
        }
        entrypointPostInit.clear();
        for (Address address : addressPostInit) {
            address.postInit();
        }
        addressPostInit.clear();
        for (Kingdom kingdom : kingdomPostInit) {
            kingdom.postInit();
        }
        kingdomPostInit.clear();
    }

    public static void loadCitizens(boolean respawn) {
        if (citizenPostInit.size() == 0) {return;}
        int loadedCitizens = 0;
        for (Citizen citizen : citizenPostInit) {
            citizenPostInit.remove(citizen);
            citizen.load(respawn);
            if (!citizenPostInit.contains(citizen)) {
                loadedCitizens++;
            }
        }
        if (loadedCitizens > 0) {
            if (citizenPostInit.size() == 0) {
                Bukkit.broadcastMessage("[Kingdoms] " + ChatColor.GREEN + "Successfully loaded " + loadedCitizens + " citizens");
            } else {
                Bukkit.broadcastMessage(String.format("[Kingdoms] %sSuccessfully loaded %d citizens, but failed to load %s", ChatColor.YELLOW, loadedCitizens, citizenPostInit.size()));
            }
        } else {
            Bukkit.broadcastMessage("[Kingdoms] " + ChatColor.RED + "Failed to load " + citizenPostInit.size() + " citizens");
        }
    }

    public static void scheduleKingdomInit(Kingdom kingdom) {
        kingdomPostInit.add(kingdom);
    }
    public static void scheduleEntrypointInit(Entrypoint entrypoint) {
        entrypointPostInit.add(entrypoint);
    }
    public static void scheduleAddressInit(Address address) {
        addressPostInit.add(address);
    }
    public static void scheduleCitizenInit(Citizen citizen) {citizenPostInit.add(citizen);}

    public static void addKingdom(String kingdomID, String player) {
        if (kingdoms.containsKey(kingdomID)) {
            throw new IllegalArgumentException(ChatColor.RED + "Kingdom \"" + kingdomID + "\" already exist!");
        }
        kingdoms.put(kingdomID, new Kingdom(player));
    }

    public static Kingdom getKingdom(String kingdomID) {
        Kingdom kingdom = kingdoms.get(kingdomID);
        if (kingdom == null) {
            throw new IllegalArgumentException(ChatColor.RED + "Kingdom \"" + kingdomID + "\" doesnt exist!");
        }
        return kingdom;
    }

    public static Set<String> getKingdomIDs() {
        return kingdoms.keySet();
    }

    public static void saveData() {
        if (kingdoms == null) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(kingdoms);
            FileWriter fileWriter = new FileWriter("C:\\Users\\gruve\\Desktop\\Server\\Four Kingdoms\\plugin data\\kingdoms_data.json");
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void
    loadData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File("C:\\Users\\gruve\\Desktop\\Server\\Four Kingdoms\\plugin data\\kingdoms_data.json");
            Map<String, Kingdom> kingdomMap = mapper.readValue(file, new TypeReference<>() {
            });

            // Oppretter et HashMap basert på Map
            kingdoms = new HashMap<>(kingdomMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
