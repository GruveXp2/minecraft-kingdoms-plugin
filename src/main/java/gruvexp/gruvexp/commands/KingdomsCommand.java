package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.core.Locality;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class KingdomsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);

        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        String oper = args[0];
        switch (oper) {
            case "info" -> {
                TextComponent message = Component.text("This is the kingdom server. The central meeting place is the Big Thing, to get to it run /bigthing\n")
                        .append(Component.text("Here is a list of all the current kingdoms:\n"));
                Collection<Kingdom> kingdoms = KingdomsManager.getKingdoms();

                for (Kingdom kingdom : kingdoms) {
                    message = message.append(kingdom.name().append(Component.text(": ", NamedTextColor.WHITE)
                            .append(kingdom.king())));
                }
                return message;
            }
            case "select" -> {
                if (args.length == 1) return Component.text("You must specify what kingdom to select", NamedTextColor.RED);

                String kingdomID = args[1];
                Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                if (kingdom == null) return Component.text("Kingdom \"" + kingdomID + " doesnt exist!", NamedTextColor.RED);
                if (args.length == 2) return KingdomsManager.setSelectedKingdom(p, kingdom);

                String districtID = args[2];
                District district = kingdom.getDistrict(districtID);
                if (district == null) return Component.text("District \"" + districtID + " doesnt exist!", NamedTextColor.RED);
                if (args.length == 3) return KingdomsManager.setSelectedDistrict(p, district);

                String localityID = args[3];
                Locality locality = district.getLocality(localityID);
                if (locality == null) return Component.text("Locality \"" + localityID + " doesnt exist!", NamedTextColor.RED);

                return KingdomsManager.setSelectedLocality(p, locality);
            }
            case "add" -> {
                if (args.length == 1) return Component.text("You must specify what to add (probably a kingdom)", NamedTextColor.RED);
                if (args[1].equals("kingdom")) {
                    if (args.length < 4) return Component.text("You must specify a kingdom name, a player that will be king, and the player's gender", NamedTextColor.RED);
                    String kingdomID = args[2];
                    String playerName = args[3];
                    Player king = Bukkit.getPlayer(playerName);
                    if (king == null) return Component.text("Player '" + playerName + "' either doesnt exist or isnt logged on the server", NamedTextColor.RED);
                    String gender = args[3].toLowerCase();
                    if (args.length == 5) gender += "-" + args[4];
                    boolean isMale;
                    switch (gender) {
                        case "m", "king", "male", "boi", "boy", "man", "xy", "xxy", "xyy" -> isMale = true;
                        case "f", "queen", "female", "girl", "gurl", "xx", "xxx", "45,x" -> isMale = false;
                        case "trans", "trans-man", "trans-woman", "genderqueer", "queer", "two-spirit", "genderfluid", "agender", "bigender",
                             "demiboy", "demigirl", "neutrois", "androgynous", "intergender", "maverique", "genderflux",
                             "shrek-gendered", "large-ornate-building", "other", "nonbinary", "non-binary" -> {
                            String result = "There are only 2 genders, male and female. Please try again with a valid input\n" +
                                    "(note: we do occasionally accept helicopters)";
                            Main.getPlugin().getLogger().warning(result);
                            return Component.text(result, NamedTextColor.YELLOW);
                        }
                        case "helicopter", "attack-helicopter", "apache-helicopter" -> {
                            p.sendMessage(Component.text("Greetings, O Helicopter"));
                            isMale = true; // if the helicopter is female, edit the json isMale=false manually afterward
                        }
                        default -> {
                            return Component.text("failed to parse property <gender> (your input was \"" + gender + "\")", NamedTextColor.RED);
                        }
                    }

                    return KingdomsManager.addKingdom(kingdomID, king, isMale); // return result
                } else {
                    return Component.text("Invalid argument. Must be 'kingdom'.", NamedTextColor.RED);
                }
            }
            case "remove" -> {
                if (args.length == 1) return Component.text("You must specify what to remove (probably a kingdom)", NamedTextColor.RED);
                if (args[1].equals("kingdom")) {
                    if (args.length == 2) return Component.text("You must specify what kingdom to remove", NamedTextColor.RED);
                    String kingdomID = args[2];
                    if (args.length == 3) return Component.text("You must type the password to do this action", NamedTextColor.RED);
                    String password = args[3];

                    return KingdomsManager.removeKingdom(kingdomID, password); // return result
                } else {
                    return Component.text("Invalid argument. Must be 'kingdom'.", NamedTextColor.RED);
                }
            }
            case "help" -> {
                return Component.text(command.getDescription());
            }
            default -> {
                return Component.text("Invalid argument! Must be of [info | set | add | remove]", NamedTextColor.RED);
            }
        }
    }
}
