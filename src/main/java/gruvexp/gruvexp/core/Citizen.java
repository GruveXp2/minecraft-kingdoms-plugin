package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonCreator;
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

    public final String name;
    private Kingdom kingdom;

    private Villager villager;
    public final Villager.Type type;
    private Villager.Profession profession;
    private String bio;
    private UUID uuid;
    private Coord location;

    private House home;
    private Locality workLocality;

    public Citizen(String name, Kingdom kingdom, Villager.Type type, Villager.Profession profession) {
        this.name = name;
        this.kingdom = kingdom;
        this.type = type;
        this.profession = profession;
    }

    @JsonCreator()
    private Citizen(@JsonProperty("name") String name,
                    @JsonProperty("villager") String uuid,
                    @JsonProperty("location") Coord location,
                    @JsonProperty("type") String type,
                    @JsonProperty("profession") String profession) {
        this.name = name;
        this.uuid = UUID.fromString(uuid);
        this.location = location;
        this.type = Registry.VILLAGER_TYPE.get(new NamespacedKey("minecraft", type));
        if (this.type == null) throw new IllegalArgumentException("Invalid villager type: " + type);
        this.profession = Registry.VILLAGER_PROFESSION.get(new NamespacedKey("minecraft", profession));
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
                Bukkit.broadcast(Component.text(name + " was found (prob ded), spawning new villager", NamedTextColor.YELLOW));
                villager = (Villager) Main.WORLD.spawnEntity(home.getBedPos().toLocation(Main.WORLD), EntityType.VILLAGER);
                villager.customName(Component.text(Utils.toName(name)));
                villager.setCustomNameVisible(true);
                uuid = villager.getUniqueId();
            } else {
                Main.getPlugin().getLogger().warning("Failed to load citizen: " + name);
                KingdomsManager.scheduleCitizenInit(this);
            }
        }
    }

    @JsonIgnore
    public Villager getVillager() {return villager;}

    public Villager.Profession getProfession() {
        return profession;
    }

    public Component setProfession(Villager.Profession profession) {
        this.profession = profession;

        KingdomsManager.save = true;
        return Component.text("Successfully updated profession of ").append(name())
                .append(Component.text(" to ")).append(Component.text(profession.toString()));
    }

    @JsonIgnore
    public House getHome() {
        return home;
    }

    public Component setHome(House house) {
        if (home == null && house != null) {
            villager = (Villager) Main.WORLD.spawnEntity(house.getBedPos().toLocation(Main.WORLD), EntityType.VILLAGER);
            uuid = villager.getUniqueId();
            villager.customName(Component.text(Utils.toName(name)));
            villager.setCustomNameVisible(true);
        }
        home = house;
        if (house == null) return Component.text("This villager is now homeless");


        KingdomsManager.save = true;
        return Component.text("Successfully set home of ").append(name())
                .append(Component.text(" to ")).append(house.name())
                .append(Component.text(" in ")).append(house.getLocality().getDistrict().name());
    }

    public Component setBio(String bio) {
        this.bio = bio;

        KingdomsManager.save = true;
        return Component.text("Successfully updated bio of ").append(name());
    }

    public void goToWork() {
        if (workLocality == null) {
            Bukkit.broadcast(Component.text(name + " failed to go to work, this citizen is not registered i a workplace", NamedTextColor.RED));
            return;
        }
        Location doorPos = home.getDoorPos().toLocation(Main.WORLD);
        Utils.openDoor(doorPos.getBlock());
        villager.teleport(doorPos);
        new WalkPath(villager, home.getLocality(), workLocality, null, home.getExitPath()).runTaskTimer(Main.getPlugin(), 0, 1);
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

    private boolean resolved = false;
    private String workLocalityDeferred;
    private String homeDeferred;

    public void resolveReferences(Kingdom parentKingdom) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");
        this.kingdom = parentKingdom;
        if (workLocalityDeferred != null) {
            String[] address = workLocalityDeferred.split(":");

            Kingdom kingdom = KingdomsManager.getKingdom(address[0]);
            if (kingdom == null) throw new IllegalArgumentException("Kingdom \"" + address[0] + "\" doesnt exist!");
            District district = kingdom.getDistrict(address[1]);
            if (district == null) throw new IllegalArgumentException("District \"" + address[1] + "\" doesnt exist!");
            Locality locality = district.getLocality(address[2]);
            if (locality == null) throw new IllegalArgumentException("Locality \"" + address[2] + "\" doesnt exist!");
            workLocality = locality;
            workLocalityDeferred = null;
        }
        if (homeDeferred != null) {
            String[] address = homeDeferred.split(":");

            District district = kingdom.getDistrict(address[0]);
            if (district == null) throw new IllegalArgumentException("District \"" + address[1] + "\" doesnt exist!");
            String[] houseAddress = address[1].split("-");
            Locality locality = district.getLocality(houseAddress[0]);
            if (locality == null) throw new IllegalArgumentException("Locality \"" + address[2] + "\" doesnt exist!");

            this.home = locality.getHouse(Integer.parseInt(houseAddress[1]));
            homeDeferred = null;
        }
        load(false);
        resolved = true;
    }

    @JsonProperty("bio") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getBio() {
        return bio;
    }

    @JsonProperty("type")
    private String getTypeJSON() {
        return type.toString();
    }

    @JsonProperty("profession")
    private String getProfessionJSON() {
        return profession.toString();
    }


    @JsonProperty("location") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Coord getLocationJSON() {
        if (villager == null) return null;

        Location location = villager.getLocation();
        return new Coord(location.getX(), location.getY(), location.getZ());
    }

    @JsonProperty("villager")
    private String getVillagerJSON() {
        return uuid.toString();
    }

    @JsonProperty("homeAddress")
    private String getHomeAddressJSON() {
        return home.nationalAddress();
    }

    @JsonProperty("homeAddress")
    private void setHomeAddressJSON(String homeAddress) {
        homeDeferred = homeAddress;
    }

    @JsonProperty("workAddress") @JsonInclude(JsonInclude.Include.NON_NULL)
    private String getWorkAddress() {
        return workLocality.tag();
    }

    @JsonProperty("homeAddress")
    private void setWorkAddressJSON(String workAddress) {
        workLocalityDeferred = workAddress;
    }
}
