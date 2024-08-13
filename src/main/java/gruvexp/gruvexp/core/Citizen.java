package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.path.WalkPath;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;

import java.util.UUID;

public class Citizen { //holder info om hver villager, som bosted, fabrikk, og personlige egenskaper

    private Villager VILLAGER;
    private String NAME; //villagerens fulle navn
    private final Villager.Type TYPE;
    private final Villager.Profession PROFESSION;
    private House home;
    private String homeAddress;
    private String workAddress;
    private String bio;
    private UUID uuid;
    private Coord location;
    private String kingdomID;

    @SuppressWarnings("unused")
    public Citizen(@JsonProperty("villager") String uuid, @JsonProperty("homeAddress") String homeAddress, @JsonProperty("workAddress") String workAddress,
                   @JsonProperty("location") Coord location, @JsonProperty("type") String type, @JsonProperty("profession") String profession) {
        this.workAddress = workAddress;
        this.homeAddress = homeAddress;
        this.uuid = UUID.fromString(uuid);
        this.location = location;
        TYPE = Villager.Type.valueOf(type.toUpperCase());
        PROFESSION = Villager.Profession.valueOf(profession.toUpperCase());
    }

    public Citizen(String name, Villager.Type type, Villager.Profession profession, Kingdom kingdom, String homeAddress) {
        String[] homeAddressStr = homeAddress.split(" ");
        home = kingdom.getDistrict(homeAddressStr[0]).getAddress(homeAddressStr[1]).getHouse(Integer.parseInt(homeAddressStr[2]));
        NAME = name;
        TYPE = type;
        PROFESSION = profession;
        VILLAGER = (Villager) Main.WORLD.spawnEntity(home.getBedPos().toLocation(Main.WORLD), EntityType.VILLAGER);
        uuid = VILLAGER.getUniqueId();
        VILLAGER.setCustomName(Utils.ToName(name));
        VILLAGER.setCustomNameVisible(true);
        this.homeAddress = homeAddress;
    }

    public void load(boolean respawn) {
        if (location != null) {
            Chunk chunk = location.toLocation(Main.WORLD).getChunk();
            if (!chunk.isLoaded()) {
                chunk.load();
            }
        }
        VILLAGER = (Villager) Bukkit.getEntity(this.uuid);
        if (VILLAGER == null) {
            if (respawn) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + NAME + " was found (prob ded), spawning new villager");
                VILLAGER = (Villager) Main.WORLD.spawnEntity(home.getBedPos().toLocation(Main.WORLD), EntityType.VILLAGER);
                VILLAGER.setCustomName(Utils.ToName(NAME));
                VILLAGER.setCustomNameVisible(true);
                uuid = VILLAGER.getUniqueId();
            } else {
                Bukkit.getLogger().warning("Failed to load citizen: " + NAME);
                KingdomsManager.scheduleCitizenInit(this);
            }
        }
    }

    public void postInit(String name, Kingdom kingdom) {
        kingdomID = kingdom.toString();
        NAME = name;
        String[] homeAddressStr = homeAddress.split(" ");
        home = kingdom.getDistrict(homeAddressStr[0]).getAddress(homeAddressStr[1]).getHouse(Integer.parseInt(homeAddressStr[2]));
        load(false);
    }

    @SuppressWarnings("unused") @JsonProperty("villager")
    private String getVillagerID() {
        return uuid.toString();
    }

    @JsonIgnore
    public Villager getVillager() {return VILLAGER;}

    @JsonIgnore
    public String getName() {
        return NAME;
    }

    @JsonProperty("type")
    public String getType() {
        return TYPE.toString();
    }

    @JsonProperty("profession")
    public String getProfession() {
        return PROFESSION.toString();
    }

    @JsonProperty("homeAddress")
    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        String[] homeAddressParts = homeAddress.split(" ");
        Kingdom kingdom = KingdomsManager.getKingdom(homeAddressParts[0]);
        District district = kingdom.getDistrict(homeAddressParts[1]);
        Address address = district.getAddress(homeAddressParts[2]);
        home = address.getHouse(homeAddressParts[3]);
        this.homeAddress = homeAddress;
    }

    @JsonProperty("workAddress") @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getWorkAddress() {
        return workAddress;
    }

    public void setWorkAddress(String workAddress) {
        this.workAddress = workAddress;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    @JsonProperty("bio") @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getBio() {
        return bio;
    }

    @SuppressWarnings("unused")  @JsonProperty("location") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Coord getLocation() {
        if (VILLAGER == null) {
            return null;
        }
        Location location = VILLAGER.getLocation();
        return new Coord(location.getX(), location.getY(), location.getZ());
    }

    public void goToWork() {
        if (workAddress == null) {
            Bukkit.broadcastMessage(ChatColor.RED + NAME + " failed to go to work, this citizen is not registered i a workplace");
            return;
        }
        Location doorPos = home.getDoorPos().toLocation(Main.WORLD);
        Utils.openDoor(doorPos.getBlock());
        VILLAGER.teleport(doorPos);
        String[] workAddressParts = workAddress.split(" ");
        String[] homeAddressParts = homeAddress.split(" ");
        new WalkPath(VILLAGER, kingdomID, homeAddressParts[0], homeAddressParts[1], workAddressParts[0], workAddressParts[1], workAddressParts[2], null, home.getExitPath()).runTaskTimer(Main.getPlugin(), 0, 1);
        //telporter til døra, sett addressen til jobbaddresse og begynn å gå exitPath.
    }
    // Denne classen skal lagre følgende informasjon: Bostedadresse, jobbaddresse, sengkordinater, profession(skin), variant (biome skin)
    // Lagrer ikke navn(inkl etternavn), det lagres som key i kingdom hashmappet


}
