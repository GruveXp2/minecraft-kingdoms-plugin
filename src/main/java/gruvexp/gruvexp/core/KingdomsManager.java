package gruvexp.gruvexp.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import gruvexp.gruvexp.FilePath;
import gruvexp.gruvexp.rail.Entrypoint;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public final class KingdomsManager {

    public static final ImmutableSet<String> RAIL_SHAPES = ImmutableSet.of("north_south", "east_west", "north_east", "north_west", "south_east", "south_west");
    public static final ImmutableSet<String> ROUTES = ImmutableSet.of("forward", "right", "left");
    public static final ImmutableSet<String> SIGNS = ImmutableSet.of("forward", "right", "left", "forward_roundabout", "right_roundabout", "left_roundabout", "u_turn");
    public static final ImmutableSet<String> DIRECTIONS = ImmutableSet.of("n", "s", "e", "w");
    private static HashMap<String, Kingdom> kingdoms;
    public static final HashSet<String> BLOCKS = new HashSet<>();
    public static boolean save = false;

    private static final HashSet<Entrypoint> entrypointPostInit = new HashSet<>();
    private static final HashSet<Locality> LOCALITY_POST_INIT = new HashSet<>();
    private static final HashSet<Kingdom> kingdomPostInit = new HashSet<>();
    private static final HashSet<Citizen> citizenPostInit = new HashSet<>();

    private static final HashMap<Player, Kingdom> selectedKingdom = new HashMap<>();
    private static final HashMap<Player, District> selectedDistrict = new HashMap<>();
    private static final HashMap<Player, Locality> selectedLocality = new HashMap<>();

    public static Kingdom getSelectedKingdom(Player p) {
        return selectedKingdom.get(p);
    }
    public static District getSelectedDistrict(Player p) {
        return selectedDistrict.get(p);
    }
    public static Locality getSelectedLocality(Player p) {
        return selectedLocality.get(p);
    }
    public static void setSelectedKingdom(Player p, Kingdom kingdom) {
        selectedKingdom.put(p, kingdom);
        selectedDistrict.remove(p);
        selectedLocality.remove(p);
    }
    public static void setSelectedDistrict(Player p, District district) {
        selectedKingdom.put(p, district.getKingdom());
        selectedDistrict.put(p, district);
        selectedLocality.remove(p);
    }
    public static void setSelectedLocality(Player p, Locality locality) {
        selectedKingdom.put(p, locality.getDistrict().getKingdom());
        selectedDistrict.put(p, locality.getDistrict());
        selectedLocality.put(p, locality);
    }

    public static void init() {
        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                BLOCKS.add(material.toString().toLowerCase());
            }
        }
        for (Entrypoint entrypoint : entrypointPostInit) {
            entrypoint.init();
        }
        entrypointPostInit.clear();
        for (Locality locality : LOCALITY_POST_INIT) {
            locality.postInit();
        }
        LOCALITY_POST_INIT.clear();
        for (Kingdom kingdom : kingdomPostInit) {
            kingdom.postInit();
        }
        kingdomPostInit.clear();
    }

    public static void loadCitizens(boolean respawn) {
        if (citizenPostInit.isEmpty()) {return;}
        int loadedCitizens = 0;
        for (Citizen citizen : citizenPostInit) {
            citizenPostInit.remove(citizen);
            citizen.load(respawn);
            if (!citizenPostInit.contains(citizen)) {
                loadedCitizens++;
            }
        }
        if (loadedCitizens > 0) {
            if (citizenPostInit.isEmpty()) {
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
    public static void scheduleAddressInit(Locality locality) {
        LOCALITY_POST_INIT.add(locality);
    }
    public static void scheduleCitizenInit(Citizen citizen) {citizenPostInit.add(citizen);}

    public static TextComponent addKingdom(String ID, Player king, boolean isMale) {
        if (kingdoms.containsKey(ID)) return Component.text("Kingdom \"" + ID + "\" already exists!", NamedTextColor.RED);

        kingdoms.put(ID, new Kingdom(ID, king.getUniqueId(), isMale));
        return Component.text("Kingdom ")
                .append(Component.text(ID, NamedTextColor.GOLD))
                .append(Component.text(" successfully added", NamedTextColor.GREEN))
                .append(Component.text(" with "))
                .append(king.name().color(NamedTextColor.YELLOW))
                .append(Component.text(" as " + (isMale ? "king" : "queen")));
    }

    public static Kingdom getKingdom(String kingdomID) {
        return kingdoms.get(kingdomID);
    }

    public static TextComponent removeKingdom(String kingdomID, String password) {
        Kingdom kingdom = kingdoms.get(kingdomID);
        if (kingdom == null) {
            return Component.text("Kingdom \"" + kingdomID + "\" doesnt exist!", NamedTextColor.RED);
        }
        if (!Objects.equals(password, "kjør_kano_det_forurenser_ikke")) return Component.text("Wrong password", NamedTextColor.RED);
        return Component.text("Successfully removed kingdom ")
                .append(Component.text(kingdomID, NamedTextColor.GOLD))
                .append(Component.text(". TO UNDO THIS ACTION, BACKUP THE JSON FILE BEFORE THE SERVER CLOSES"));
    }

    public static Set<String> getKingdomIDs() {
        return kingdoms.keySet();
    }

    public static Collection<Kingdom> getKingdoms() {
        return kingdoms.values();
    }

    public static void saveData() {
        if (kingdoms == null) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(kingdoms);
            FileWriter fileWriter = new FileWriter(FilePath.SERVER_FOLDER + FilePath.SERVER_NAME + "\\plugin data\\kingdoms_data.json");
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(FilePath.SERVER_FOLDER + FilePath.SERVER_NAME + "\\plugin data\\kingdoms_data.json");
            Map<String, Kingdom> kingdomMap = mapper.readValue(file, new TypeReference<>() {
            });

            // Oppretter et HashMap basert på Map
            kingdoms = new HashMap<>(kingdomMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
