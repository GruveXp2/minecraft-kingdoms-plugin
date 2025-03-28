package gruvexp.gruvexp;


import java.util.HashMap;
import java.util.Map;

public class HomeManager {

    public static final HashMap<String, String> PlayerToHomeAdr = new HashMap<>(); // key=username, val=koordinater

    public static void loadData() {
        String input = Utils.loadTxt("homes");
        String[] homes = input.split("\n");

        for (String home : homes) {
            String player = home.substring(0, home.indexOf(":"));
            String loc = home.substring(home.indexOf(":") + 2);
            PlayerToHomeAdr.put(player, loc);
        }


    }

    public static void saveData() {
        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, String> set : PlayerToHomeAdr.entrySet()) {
            output.append(set.getKey()).append(": ").append(set.getValue()).append("\n");
        }
        Utils.saveTxt("homes", output.toString());
    }

}
