package gruvexp.gruvexp.path;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.rail.Coord;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;
import java.util.stream.Collectors;

public class Path {

    public static final HashSet<String> DIRECTIONS = new HashSet<>(Arrays.asList("n", "s", "e", "w", "ne", "nw", "se", "sw"));

    public final String id;
    private Locality locality;

    private Coord startPos;
    private HashMap<Integer, Character> turns; // på hvilken retning man skal svinge på index x. retninger: NSEW ZCUM(NE ES SW WN). hver gang man mover, så skjekker man om turns.get(counter) ikke er null, og hvis den ikke er det, så flytter man seg i den retninga.

    private final HashMap<Integer, PathBranch> branches = new HashMap<>();

    public Path(String id, Locality parentLocality, Coord startPos) {
        this.id = id;
        this.locality = parentLocality;
        this.startPos = startPos;
    }

    @JsonCreator
    private Path(@JsonProperty("id") String id,
                 @JsonProperty("startPos") Coord startPos,
                 @JsonProperty("turns") HashMap<Integer, Character> turns) {
        this.id = id;
        this.startPos = startPos;
        this.turns = turns;
    }

    @JsonIgnore
    public Locality getLocality() {
        return locality;
    }

    public static char dirToChar(String direction) {
        return switch (direction) {
            case "n":
            case "s":
            case "e":
            case "w":
                yield  direction.toCharArray()[0];
            case "ne":
                yield 'N';
            case "se":
                yield 'E';
            case "sw":
                yield 'S';
            case "nw":
                yield 'W';
            default:
                throw new IllegalArgumentException("Illegal direction value!");
        };
    }

    public static String dirToStr(char direction) {
        return switch (direction) {
            case 'n':
            case 's':
            case 'e':
            case 'w':
                yield String.valueOf(direction);
            case 'N':
                yield "ne";
            case 'E':
                yield "se";
            case 'S':
                yield "sw";
            case 'W':
                yield "nw";
            default:
                throw new IllegalArgumentException("Illegal direction value!");
        };
    }

    public Coord getStartPos() {
        return startPos;
    }

    public Component setStartPos(Coord startPos) {
        this.startPos = startPos;

        KingdomsManager.save = true;
        return Component.text("Successfully set start pos of ").append(name())
                .append(Component.text(" to ")).append(startPos.name());
    }

    public Component addBranch(int index, Path targetPath, int enterIndex, HashSet<String> addresses) {
        branches.put(index, new PathBranch(targetPath, enterIndex, addresses));

        KingdomsManager.save = true;
        return Component.text("Successfully added branch at ").append(nameIndex(index).appendNewline())
                .append(Component.text("entering path section ")).append(targetPath.nameIndex(enterIndex)).appendNewline()
                .append(Component.text("with addresses ")).append(Component.text(String.join(", ", addresses), NamedTextColor.GOLD));
    }

    public Character getTurn(int index) {
        return turns.get(index);
    } // getter en sving ordre dersom det er en sving på den indexen

    public HashMap<Integer, Character> getTurns() {
        return turns;
    }

    public Component setTurns(HashMap<Integer, Character> turns) {
        this.turns = turns;

        KingdomsManager.save = true;
        return Component.text("Successfully set turn data for path section ").append(name());
    }

    public PathBranch getBranch(int index) {
        return branches.get(index);
    }

    public boolean hasBranch(int index) {return branches.containsKey(index);}

    public boolean hasBranches() {return !branches.isEmpty();}

    public boolean isEndpoint(int index) {
        return Collections.max(branches.keySet()) == index;
    }

    public Component removeBranch(int index) {
        if (!branches.containsKey(index)) return Component.text("Nothing happened, this path has no branch at index " + index, NamedTextColor.YELLOW);

        branches.remove(index);

        KingdomsManager.save = true;
        return Component.text("Successfully removed branch at ").append(nameIndex(index));
    }

    public Component name() {
        return Component.text(id, NamedTextColor.YELLOW);
    }

    public Component nameIndex(int index) {
        return name().append(Component.text("", NamedTextColor.WHITE)).append(Component.text(index, NamedTextColor.BLUE));
    }

    public Component turns() {
        return turns.isEmpty() ? Component.text("missing", NamedTextColor.YELLOW)
                : Component.text(getTurns().entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // sortér etter nøkkel
                .map(e -> e.getKey() + ":" + e.getValue())
                .collect(Collectors.joining(" ")), NamedTextColor.BLUE);
    }

    public Component branches() {
        Component branchInfo = branches.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    int id = entry.getKey();
                    PathBranch branch = entry.getValue();

                    return (Component) Component.text(id, NamedTextColor.BLUE).append(Component.text(" -> ", NamedTextColor.WHITE)
                            .append(branch.path().name())
                            .append(Component.text(":" + branch.enterIndex() + " for "))
                            .append(Component.text(String.join(", ", branch.addresses()), NamedTextColor.GOLD))
                            .append(Component.newline()));
                })
                .reduce(Component.empty(), Component::append);

        return branches.isEmpty() ? Component.text("none", NamedTextColor.YELLOW)
                : branchInfo;
    }

    private boolean resolved = false;
    Map<Integer, Map<String, Object>> branchesDeferred;

    public void resolveReferences(Locality parentLocality) {
        if (resolved) throw new IllegalStateException("Tried to resolve references a second time, but resolving should only be done once!");

        this.locality = parentLocality;
        if (branchesDeferred != null) {
            for (Map.Entry<Integer, Map<String, Object>> entry : branchesDeferred.entrySet()) {
                int index = entry.getKey();
                Map<String, Object> branchData = entry.getValue();
                String pathID = (String) branchData.get("path");
                int enterIndex = (int) branchData.get("enterIndex");
                HashSet<String> addresses = new HashSet<>((ArrayList<String>) branchData.get("addresses"));
                PathBranch branch = new PathBranch(locality.getPath(pathID), enterIndex, addresses);
                branches.put(index, branch);
            }
            branchesDeferred = null;
        }
        resolved = true;
    }

    @JsonProperty("branches") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Map<String, Object>> getBranchesJSON() {
        if (branches.isEmpty()) return null;

        Map<Integer, Map<String, Object>> branches = new HashMap<>();
        for (Map.Entry<Integer, PathBranch> entry : this.branches.entrySet()) {
            int index = entry.getKey();
            PathBranch branchData = entry.getValue();
            Map<String, Object> branch = new HashMap<>();
            branch.put("path", branchData.path().id);
            branch.put("enterIndex", branchData.enterIndex());
            branch.put("addresses", branchData.addresses());
            branches.put(index, branch);
        }
        return branches;
    }

    @JsonProperty("branches")
    private void setBranchesJSON(Map<Integer, Map<String, Object>> branches) {
        branchesDeferred = branches;
    }
}
