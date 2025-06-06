package gruvexp.gruvexp.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import gruvexp.gruvexp.FilePath;
import gruvexp.gruvexp.Main;
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

    // make all these sets into enums (much better)
    public static final ImmutableSet<String> RAIL_SHAPES = ImmutableSet.of("north_south", "east_west", "north_east", "north_west", "south_east", "south_west");
    public static final ImmutableSet<String> ROUTES = ImmutableSet.of("forward", "right", "left");
    public static final ImmutableSet<String> SIGNS = ImmutableSet.of("forward", "right", "left", "forward_roundabout", "right_roundabout", "left_roundabout", "u_turn");
    public static final ImmutableSet<String> DIRECTIONS = ImmutableSet.of("n", "s", "e", "w");
    private static HashMap<String, Kingdom> kingdoms;
    public static final HashSet<String> BLOCKS = new HashSet<>();
    private static boolean save = false;

    private static final HashSet<Citizen> citizenPostInit = new HashSet<>();

    private static final HashMap<Player, Kingdom> selectedKingdom = new HashMap<>();
    private static final HashMap<Player, District> selectedDistrict = new HashMap<>();
    private static final HashMap<Player, Locality> selectedLocality = new HashMap<>();

    private static final HashMap<Class<?>, Integer> edits = new HashMap<>();

    public static Kingdom getSelectedKingdom(Player p) {
        if (selectedKingdom.get(p) == null) {
            UUID playerID = p.getUniqueId();
            for (Kingdom kingdom : getKingdoms()) {
                if (kingdom.getKingID().equals(playerID)) {
                    setSelectedKingdom(p, kingdom);
                    break;
                }
            }
        }
        return selectedKingdom.get(p);
    }
    public static District getSelectedDistrict(Player p) {
        return selectedDistrict.get(p);
    }
    public static Locality getSelectedLocality(Player p) {
        return selectedLocality.get(p);
    }
    public static Component setSelectedKingdom(Player p, Kingdom kingdom) {
        if (selectedKingdom.get(p) == kingdom && selectedDistrict.get(p) == null && selectedLocality.get(p) == null)
            return Component.text("Nothing happened, you already have this kingdom selected", NamedTextColor.YELLOW);
        selectedKingdom.put(p, kingdom);
        selectedDistrict.remove(p);
        selectedLocality.remove(p);
        return Component.text("Successfully selected ")
                .append(Component.text("kingdom ", Kingdom.LABEL_COLOR)).append(kingdom.name());
    }
    public static Component setSelectedDistrict(Player p, District district) {
        if (selectedDistrict.get(p) == district && selectedLocality.get(p) == null)
            return Component.text("Nothing happened, you already have this district selected", NamedTextColor.YELLOW);
        selectedKingdom.put(p, district.getKingdom());
        selectedDistrict.put(p, district);
        selectedLocality.remove(p);
        return Component.text("Successfully selected ")
                .append(Component.text("district ", District.LABEL_COLOR)).append(district.address());
    }
    public static Component setSelectedLocality(Player p, Locality locality) {
        if (selectedLocality.get(p) == locality)
            return Component.text("Nothing happened, you already have this locality selected", NamedTextColor.YELLOW);
        selectedKingdom.put(p, locality.getDistrict().getKingdom());
        selectedDistrict.put(p, locality.getDistrict());
        selectedLocality.put(p, locality);
        return Component.text("Successfully selected ")
                .append(Component.text("locality ", Locality.LABEL_COLOR)).append(locality.address());
    }

    public static void init() {
        for (Material material : Material.values()) {
            if (!material.isLegacy()) {
                BLOCKS.add(material.toString().toLowerCase());
            }
        }
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

    public static void scheduleCitizenInit(Citizen citizen) {citizenPostInit.add(citizen);}

    public static TextComponent addKingdom(String ID, Player king, boolean isMale) {
        if (kingdoms.containsKey(ID)) return Component.text("Kingdom \"" + ID + "\" already exists!", NamedTextColor.RED);

        kingdoms.put(ID, new Kingdom(ID, king.getUniqueId(), isMale));

        KingdomsManager.registerEdit(KingdomsManager.class);
        return Component.text("Successfully added ").append(Component.text("new kingdom ", Kingdom.LABEL_COLOR))
                .append(Component.text(ID, Kingdom.VALUE_COLOR))
                .append(Component.text(" with "))
                .append(king.name().color(NamedTextColor.YELLOW))
                .append(Component.text(" as " + (isMale ? "king" : "queen")));
    }

    public static Kingdom getKingdom(String kingdomID) {
        return kingdoms.get(kingdomID);
    }

    public static Collection<Kingdom> getKingdoms() {
        return kingdoms.values();
    }

    public static Set<String> getKingdomIDs() {
        return kingdoms.keySet();
    }

    public static TextComponent removeKingdom(String kingdomID, String password) {
        Kingdom kingdom = kingdoms.get(kingdomID);
        if (kingdom == null) {
            return Component.text("Kingdom \"" + kingdomID + "\" doesnt exist!", NamedTextColor.RED);
        }
        if (!Objects.equals(password, "kjør_kano_det_forurenser_ikke")) return Component.text("Wrong password", NamedTextColor.RED);
        kingdoms.remove(kingdomID);

        KingdomsManager.registerEdit(KingdomsManager.class);
        return Component.text("Successfully removed ").append(Component.text("kingdom ", Kingdom.LABEL_COLOR))
                .append(Component.text(kingdomID, Kingdom.VALUE_COLOR))
                .append(Component.text(". TO UNDO THIS ACTION, BACKUP THE JSON FILE BEFORE THE SERVER CLOSES", NamedTextColor.RED));
    }

    public static void registerEdit(Object object) {
        edits.merge(object.getClass(), 1, Integer::sum);
        save = true;
    }

    public static void loadData() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            File file = new File(FilePath.SERVER_FOLDER + FilePath.SERVER_NAME + "\\plugin data\\kingdoms_data.json");
            Set<Kingdom> kingdomSet = mapper.readValue(file, new TypeReference<>() {
            });

            kingdoms = new HashMap<>();
            kingdomSet.forEach(kingdom -> kingdoms.put(kingdom.id, kingdom));
            kingdomSet.forEach(Kingdom::resolveReferences);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveData() {
        if (kingdoms == null || !save) {
            return;
        }
        Main.getPlugin().getLogger().info("Saving changes...");
        for (var entry : edits.entrySet()) {
            Main.getPlugin().getLogger().info(" - " + entry.getKey().getSimpleName() + ": " + entry.getValue() + " edits");
        }

        ObjectMapper mapper = new ObjectMapper();
        try {
            String json = mapper.writeValueAsString(kingdoms.values());
            FileWriter fileWriter = new FileWriter(FilePath.SERVER_FOLDER + FilePath.SERVER_NAME + "\\plugin data\\kingdoms_data.json");
            fileWriter.write(json);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
