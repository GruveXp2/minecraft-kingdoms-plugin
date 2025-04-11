package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;

public class House {

    public final int nr;
    private final Locality locality;
    private Coord doorPos;
    private Coord bedPos; //i framtida en liste over senger
    private Path exitPath;
    private String exitPathID;
    private HashSet<Citizen> residents = new HashSet<>(8); // når en villager blir adda med et house objekt, så tar man house.addMember(villager)

    public House(int nr, Locality locality) {
        this.nr = nr;
        this.locality = locality;
    }
    // i fremtida liste over paths inni huset, som feks path fra senga til døra

    public void postInit(Locality locality) {
        if (exitPathID == null) {return;}
        exitPath = locality.getPath(exitPathID);
    }

    public Locality getLocality() {
        return locality;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public HashSet<Citizen> getResidents() {
        if (residents.isEmpty()) {
            return null;
        }
        return residents;
    }

    @SuppressWarnings("unused") @JsonProperty("residents")
    private void setResident(HashSet<Citizen> citizens) {
        this.residents = citizens;
    }

    public Component addResident(Citizen citizen) {
        if (residents.contains(citizen)) return Component.text("Nothing happened, that citizen was already a resident of this house", NamedTextColor.YELLOW);

        residents.add(citizen);

        KingdomsManager.save = true;
        return Component.text("Successfully added resident ").append(citizen.name())
                .append(Component.text(" to ")).append(name());
    }

    public Component removeResident(Citizen citizen) {
        if (!residents.contains(citizen)) return citizen.name().append(Component.text(" isnt a resident of this house", NamedTextColor.YELLOW));

        residents.remove(citizen);

        KingdomsManager.save = true;
        return Component.text("Successfully removed resident ").append(citizen.name())
                .append(Component.text(" from ")).append(name());
    }

    public Component setDoorPos(Coord doorPos) {
        this.doorPos = doorPos;

        KingdomsManager.save = true;
        return Component.text("Successfully set door pos of ").append(name())
                .append(Component.text(" to ")).append(doorPos.name());
    }

    public Coord getDoorPos() {
        return doorPos;
    }

    public Component setBedPos(Coord bedPos) {
        this.bedPos = bedPos;

        KingdomsManager.save = true;
        return Component.text("Successfully set bed pos of ").append(name())
                .append(Component.text(" to ")).append(bedPos.name());
    }

    public Coord getBedPos() {
        return bedPos;
    }

    @JsonIgnore
    public Path getExitPath() {
        return exitPath;
    }

    @SuppressWarnings("unused") @JsonProperty("exitPath") @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getExitPathID() {
        if (exitPath == null) {
            return null;
        }
        return exitPath.toString();
    }

    @JsonIgnore
    public Component setExitPath(Path exitPath) {
        this.exitPath = exitPath;

        KingdomsManager.save = true;
        return Component.text("Successfully set exith path section of ").append(name())
                .append(Component.text(" to ")).append(exitPath.name());
    }

    @SuppressWarnings("unused") @JsonProperty("exitPath")
    public void setExitPath(String exitPathID) {
        this.exitPathID = exitPathID;
    }

    public Component name() {
        return locality.name().appendSpace().append(Component.text(nr, NamedTextColor.BLUE));
    }

    public String nationalAddress() {
        return locality.getDistrict().id + ":" + locality.id + "-" + nr;
    }
}
