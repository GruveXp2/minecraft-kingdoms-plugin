package gruvexp.gruvexp.core;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class House {

    public final int nr;
    private Locality locality;

    private Coord doorPos;
    private Coord bedPos; //i framtida en liste over senger
    private Path exitPath;

    private final HashSet<Citizen> residents = new HashSet<>(8); // når en villager blir adda med et house objekt, så tar man house.addMember(villager)

    public House(int nr, Locality locality) {
        this.nr = nr;
        this.locality = locality;
    }

    @JsonCreator
    private House(@JsonProperty("nr") int nr,
                  @JsonProperty("doorPos") Coord doorPos,
                  @JsonProperty("bedPos") Coord bedPos) {
        this.nr = nr;
        this.doorPos = doorPos;
        this.bedPos = bedPos;
    }
    // i framtida liste over paths inni huset, som feks path fra senga til døra

    @JsonIgnore
    public Locality getLocality() {
        return locality;
    }

    @JsonIgnore
    public HashSet<Citizen> getResidents() {
        return residents;
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

    public Coord getDoorPos() {
        return doorPos;
    }

    public Component setDoorPos(Coord doorPos) {
        this.doorPos = doorPos;

        KingdomsManager.save = true;
        return Component.text("Successfully set door pos of ").append(name())
                .append(Component.text(" to ")).append(doorPos.name());
    }

    public Coord getBedPos() {
        return bedPos;
    }

    public Component setBedPos(Coord bedPos) {
        this.bedPos = bedPos;

        KingdomsManager.save = true;
        return Component.text("Successfully set bed pos of ").append(name())
                .append(Component.text(" to ")).append(bedPos.name());
    }

    @JsonIgnore
    public Path getExitPath() {
        return exitPath;
    }

    public Component setExitPath(Path exitPath) {
        this.exitPath = exitPath;

        KingdomsManager.save = true;
        return Component.text("Successfully set exith path section of ").append(name())
                .append(Component.text(" to ")).append(exitPath.name());
    }

    public Component name() {
        return locality.name().appendSpace().append(Component.text(nr, NamedTextColor.BLUE))
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/house " + nr + " info"));
    }

    public String nationalAddress() {
        return locality.getDistrict().id + ":" + locality.id + "-" + nr;
    }

    private boolean resolved = false;
    private String exitPathDeferred;
    HashSet<String> residentsDeferred;

    public void resolveReferences(Locality parentLocality) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");

        this.locality = parentLocality;
        if (exitPathDeferred != null) {
            exitPath = locality.getPath(exitPathDeferred);
            exitPathDeferred = null;
        }
        if (residentsDeferred != null) {
            for (String citizenName : residentsDeferred) {
                Kingdom kingdom = locality.getDistrict().getKingdom();
                Citizen resident = kingdom.getCitizen(citizenName);
                residents.add(resident);
            }
            residentsDeferred = null;
        }
        resolved = true;
    }

    @JsonProperty("exitPath") @JsonInclude(JsonInclude.Include.NON_NULL)
    public String getExitPathJSON() {
        if (exitPath == null) return null;
        return exitPath.id;
    }

    @JsonProperty("exitPath")
    private void setExitPathJSON(String exitPath) {
        this.exitPathDeferred = exitPath;
    }

    @JsonProperty("residents") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Set<String> getResidentsJSON() {
        if (residents.isEmpty()) return null;
        return residents.stream().map(citizen -> citizen.name).collect(Collectors.toSet());
    }

    @JsonProperty("residents")
    private void setResidentsJSON(HashSet<String> residents) {
        this.residentsDeferred = residents;
    }
}
