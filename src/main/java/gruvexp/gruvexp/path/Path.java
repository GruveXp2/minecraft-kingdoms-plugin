package gruvexp.gruvexp.path;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.ChatColor;

import java.util.*;

public class Path {

    public static final HashSet<String> DIRECTIONS = new HashSet<>(Arrays.asList("n", "s", "e", "w", "ne", "nw", "se", "sw"));
    @JsonIgnore
    private String ID; // pathID
    private Coord startPos;
    private HashMap<Integer, Character> turns; // på hvilken retning man skal svinge på index x. retninger: NSEW ZCUM(NE ES SW WN). hver gang man mover, så skjekker man om turns.get(counter) ikke er null, og hvis den ikke er det, så flytter man seg i den retninga.
    private final HashMap<Integer, String> branchPathID; // etter å ha gått x bloccs, er det en utgang på sida som går til en bestemt path
    private final HashMap<Integer, Integer> branchEnterIndex; // forteller hvor man kommer inn på pathen. null val = default val = 0.
    private final HashMap<Integer, HashSet<String>> branchAddresses; // pathen etter x bloccs har en metylgruppe som går til en annen path. branchAddresses.get(blocc som man er på nå), if not null så er det en branch der, skjekk addressene

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
                throw new IllegalArgumentException(ChatColor.RED + "Illegal direction value!");
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
                throw new IllegalArgumentException(ChatColor.RED + "Illegal direction value!");
        };
    }

    public Path(String ID, Coord startPos, HashMap<Integer, Character> turns) {
        this.startPos = startPos;
        this.turns = turns;
        this.ID = ID;
        branchPathID = new HashMap<>();
        branchAddresses = new HashMap<>();
        branchEnterIndex = new HashMap<>();
    }

    @SuppressWarnings("unused")
    public Path(@JsonProperty("startPos") Coord startPos, @JsonProperty("turns") HashMap<Integer, Character> turns) {
        this.startPos = startPos;
        this.turns = turns;
        branchPathID = new HashMap<>();
        branchAddresses = new HashMap<>();
        branchEnterIndex = new HashMap<>();
    }

    public void initID(String ID) {
        if (this.ID == null) {
            this.ID = ID;
        }
    }

    public Coord getStartPos() {
        return startPos;
    }

    public void setStartPos(Coord startPos) {
        this.startPos = startPos;
    }

    public void addBranch(int index, String pathID, int enterIndex, HashSet<String> branchAddress) {
        branchAddresses.put(index, branchAddress);
        branchPathID.put(index, pathID);
        branchEnterIndex.put(index, enterIndex);
    }

    public boolean hasBranch(int index) {return branchPathID.containsKey(index);}

    public boolean hasBranches() {return branchPathID.size() > 0;}

    @JsonIgnore
    public HashSet<String> getBranchAddresses(int index) {return branchAddresses.get(index);}

    @JsonIgnore
    public String getBranchPathID(int index) {return branchPathID.get(index);}

    @JsonIgnore
    public HashMap<Integer, String> getBranchPathIDs() {
        return branchPathID;
    }

    public int getBranchEnterIndex(int index) {return branchEnterIndex.get(index);}

    @SuppressWarnings("unused") @JsonProperty("branches") @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<Integer, Map<String, Object>> getBranches() {
        if (branchPathID.size() == 0) {
            return null;
        }
        Map<Integer, Map<String, Object>> branches = new HashMap<>();
        for (int index : branchPathID.keySet()) {
            Map<String, Object> branch = new HashMap<>();
            branch.put("path", branchPathID.get(index));
            branch.put("enterIndex", branchEnterIndex.get(index));
            branch.put("addresses", branchAddresses.get(index));
            branches.put(index, branch);
        }
        return branches;
    }

    @SuppressWarnings("unused") @JsonProperty("branches")
    private void setBranches(Map<Integer, Map<String, Object>> branches) {
        for (Map.Entry<Integer, Map<String, Object>> branch : branches.entrySet()) {
            int index = branch.getKey();
            Map<String, Object> branchData = branch.getValue();
            String pathID = (String) branchData.get("path");
            int enterIndex = (int) branchData.get("enterIndex");
            HashSet<String> addresses = new HashSet<>((ArrayList<String>) branchData.get("addresses"));
            branchPathID.put(index, pathID);
            branchEnterIndex.put(index, enterIndex);
            branchAddresses.put(index, addresses);
        }
    }

    public Character getTurn(int index) {
        return turns.get(index);
    } // getter en sving ordre dersom det er en sving på den indexen

    public HashMap<Integer, Character> getTurns() {
        return turns;
    }

    public void setTurns(HashMap<Integer, Character> turns) {
        this.turns = turns;
    }

    public boolean isEndpoint(int index) {
        return Collections.max(branchPathID.keySet()) == index;
    }

    @Override
    public String toString() {
        return ID;
    }
}
