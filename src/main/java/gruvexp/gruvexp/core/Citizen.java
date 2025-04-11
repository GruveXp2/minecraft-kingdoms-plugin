package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.path.WalkPath;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.UUID;

public class Citizen { //holder info om hver villager, som bosted, fabrikk, og personlige egenskaper

    public final String name; //villagerens fulle navn
    private Kingdom kingdom;
    private Villager villager;
    private final Villager.Type type;
    private Villager.Profession profession;
    private House home;
    private Locality workLocality;
    private String bio;
    private UUID uuid;
    private Coord location;

    @SuppressWarnings("unused")
    public Citizen(String name, @JsonProperty("villager") String uuid, @JsonProperty("homeAddress") String homeAddress, @JsonProperty("workAddress") String workAddress,
                   @JsonProperty("location") Coord location, @JsonProperty("type") String type, @JsonProperty("profession") String profession) {
        this.name = name;
        this.workLocality = workAddress;
        this.homeAddress = homeAddress;
        this.uuid = UUID.fromString(uuid);
        this.location = location;
        this.type = Registry.VILLAGER_TYPE.get(new NamespacedKey("minecraft", type));
        this.profession = Registry.VILLAGER_PROFESSION.get(new NamespacedKey("minecraft", profession));
    }

    public Citizen(String name, Kingdom kingdom, Villager.Type type, Villager.Profession profession) {
        this.name = name;
        this.kingdom = kingdom;
        this.type = type;
        this.profession = profession;
        villager = (Villager) Main.WORLD.spawnEntity(home.getBedPos().toLocation(Main.WORLD), EntityType.VILLAGER);
        uuid = villager.getUniqueId();
        villager.setCustomName(Utils.toName(name));
        villager.setCustomNameVisible(true);
    }

    public Kingdom getKingdom() {
        return kingdom;
    }

    public void load(boolean respawn) {
        if (location != null) {
            Chunk chunk = location.toLocation(Main.WORLD).getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
        }
        villager = (Villager) Bukkit.getEntity(this.uuid);
        if (villager == null) {
            if (respawn) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + name + " was found (prob ded), spawning new villager");
                villager = (Villager) Main.WORLD.spawnEntity(home.getBedPos().toLocation(Main.WORLD), EntityType.VILLAGER);
                villager.setCustomName(Utils.toName(name));
                villager.setCustomNameVisible(true);
                uuid = villager.getUniqueId();
            } else {
                Main.getPlugin().getLogger().warning("Failed to load citizen: " + name);
                KingdomsManager.scheduleCitizenInit(this);
            }
        }
    }

    public void postInit(String name, Kingdom kingdom) {
        kingdomID = kingdom.toString();
        this.name = name;
        String[] homeAddressStr = homeAddress.split(" ");
        home = kingdom.getDistrict(homeAddressStr[0]).getLocality(homeAddressStr[1]).getHouse(Integer.parseInt(homeAddressStr[2]));
        load(false);
    }

    @SuppressWarnings("unused") @JsonProperty("villager")
    private String getVillagerID() {
        return uuid.toString();
    }

    @JsonIgnore
    public Villager getVillager() {return villager;}

    @JsonIgnore
    public String getName() {
        return name;
    }

    @JsonProperty("type")
    public String getType() {
        return type.toString();
    }

    @JsonProperty("profession")
    public String getProfession() {
        return profession.toString();
    }

    public Component setProfession(Villager.Profession profession) {
        this.profession = profession;
        return Component.text("Successfully updated profession of ").append(name())
                .append(Component.text(" to ")).append(Component.text(profession.toString()));
    }

    @JsonProperty("homeAddress")
    public String getHomeAddress() {
        return home.nationalAddress();
    }

    public Component setHome(House house) {
        home = house;
        return Component.text("Successfully set home of ").append(name())
                .append(Component.text(" to ")).append(house.name())
                .append(Component.text(" in ")).append(house.getLocality().getDistrict().name());
    }

    @JsonProperty("workAddress") @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getWorkAddress() {
        return workLocality.tag();
    }

    public Component setBio(String bio) {
        this.bio = bio;
        return Component.text("Successfully updated bio of ").append(name());
    }

    @JsonProperty("bio") @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBio() {
        return bio;
    }

    @SuppressWarnings("unused")  @JsonProperty("location") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Coord getLocation() {
        if (villager == null) {
            return null;
        }
        Location location = villager.getLocation();
        return new Coord(location.getX(), location.getY(), location.getZ());
    }

    public void goToWork() {
        if (workLocality == null) {
            Bukkit.broadcastMessage(ChatColor.RED + name + " failed to go to work, this citizen is not registered i a workplace");
            return;
        }
        Location doorPos = home.getDoorPos().toLocation(Main.WORLD);
        Utils.openDoor(doorPos.getBlock());
        villager.teleport(doorPos);
        String[] workAddressParts = workLocality.split(" ");
        String[] homeAddressParts = homeAddress.split(" ");
        new WalkPath(villager, kingdom, homeAddressParts[0], homeAddressParts[1], workAddressParts[0], workAddressParts[1], workAddressParts[2], null, home.getExitPath()).runTaskTimer(Main.getPlugin(), 0, 1);
        //telporter til døra, sett addressen til jobbaddresse og begynn å gå exitPath.
    }
    // Denne classen skal lagre følgende informasjon: Bostedadresse, jobbaddresse, sengkordinater, profession(skin), variant (biome skin)
    // Lagrer ikke navn(inkl etternavn), det lagres som key i kingdom hashmappet <== total bs

    public Component name() {
        return Component.text(name, NamedTextColor.GREEN);
    }

    public Component workAddress() {
        Kingdom kingdom = workLocality.getDistrict().getKingdom();
        return (kingdom != this.kingdom ? kingdom.name().append(Component.text(":")) : Component.empty())
                .append(workLocality.getDistrict().name().append(Component.text(":")))
                .append(workLocality.name());
    }

    public Component homeAddress() {
        Locality locality = home.getLocality();
        District district = locality.getDistrict();
        Kingdom kingdom = district.getKingdom();
        return (kingdom != this.kingdom ? kingdom.name().append(Component.text(":")) : Component.empty())
                .append(district.name().append(Component.text(":")))
                .append(locality.name().append(Component.text(":")))
                .append(Component.text(home.nr));
    }

    public Component bio() {
        return Component.text(bio);
    }
}
