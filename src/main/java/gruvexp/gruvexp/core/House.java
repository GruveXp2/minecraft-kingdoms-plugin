package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;

import java.util.HashSet;

public class House {

    public final int nr;
    private Locality locality;
    private Coord doorPos;
    private Coord bedPos; //i framtida en liste over senger
    private Path exitPath;
    private String exitPathID;
    private HashSet<Citizen> citizens = new HashSet<>(8); // når en villager blir adda med et house objekt, så tar man house.addMember(villager)

    public House(int nr) {
        this.nr = nr;
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
    public HashSet<Citizen> getCitizens() {
        if (citizens.isEmpty()) {
            return null;
        }
        return citizens;
    }

    @SuppressWarnings("unused") @JsonProperty("citizens")
    private void setCitizens(HashSet<Citizen> citizens) {
        this.citizens = citizens;
    }

    public void addCitizen(Citizen citizen) {
        citizens.add(citizen);
    }

    public void setDoorPos(Coord doorPos) {
        this.doorPos = doorPos;
    }

    public Coord getDoorPos() {
        return doorPos;
    }

    public void setBedPos(Coord bedPos) {
        this.bedPos = bedPos;
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
    public void setExitPath(Path exitPath) {
        this.exitPath = exitPath;
    }

    @SuppressWarnings("unused") @JsonProperty("exitPath")
    public void setExitPath(String exitPathID) {
        this.exitPathID = exitPathID;
    }

    public Component address() {
        return locality.name().appendSpace().append(Component.text(nr));
    }
}
