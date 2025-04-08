package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class KingdomCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);
        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        String kingdomID = args[0];
        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
        if (kingdom == null) return Component.text("Kingdom \"" + kingdomID + "\" doesnt exist!", NamedTextColor.RED);
        if (args.length == 1) return Component.text("You must specify an operation [info | set | add | remove]");

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                Component postOffice = kingdom.getPostOfficeDistrict() != null ? kingdom.getPostOfficeDistrict().name() : Component.text("none");
                return Component.text("Kingdom of ").append(kingdom.name()).append(Component.text(":\n"))
                        .append(Component.text("Ruler: ")).append(kingdom.king()).appendNewline()
                        .append(Component.text("Post office: ")).append(postOffice)
                        .append(Component.text(kingdom.getDistrictIDs().size())).append(Component.text(" districts:\n"))
                        .append(Component.text(String.join(", ", kingdom.getDistrictIDs()))).appendNewline()
                        .append(Component.text(kingdom.getCitizenNames().size())).append(Component.text(" citizens"));
            }
            case "set" -> {
                if (args.length < 4) return Component.text("You must specify what to set and a value: set [king | color] <value>", NamedTextColor.RED);
                String property = args[2];
                switch (property) {
                    case "king" -> {
                        String playerName = args[3];
                        Player king = Bukkit.getPlayer(playerName);
                        if (king == null) return Component.text("Player \"" + playerName + "\" either doesnt exist or isnt logged on the server", NamedTextColor.RED);
                        return kingdom.setKingID(king.getUniqueId());
                    }
                    case "color" -> {
                        String colorString = args[3]; // hex
                        TextColor color = TextColor.fromHexString(colorString);
                        if (color == null) return Component.text("Invalid color format. Must be hex (#rrggbb)", NamedTextColor.RED);
                        return kingdom.setColor(color);
                    }
                    case "post office" -> {
                        String districtID = args[3];
                        District postOfficeDistrict = kingdom.getDistrict(districtID);
                        if (postOfficeDistrict == null) return Component.text("District \"" + districtID + "\" doesnt exzist!", NamedTextColor.RED);
                        return kingdom.setPostOfficeDistrict(postOfficeDistrict);
                    }
                    default -> {
                        return Component.text("Invalid property argument! Syntaks: set [king | color] <value>", NamedTextColor.RED);
                    }
                }
            }
            case "add" -> {
                if (args.length == 2) return Component.text("You must specify what to add: [citizen | district]", NamedTextColor.RED);
                String feature = args[2];
                switch (feature) {
                    case "citizen" -> {
                        if (args.length < 6) return Component.text("You must specify the details of the new citizen: add citizen <name> <variant> <profession>");
                        String name = args[3];
                        Villager.Type variant = Registry.VILLAGER_TYPE.get(new NamespacedKey("minecraft", args[4]));
                        Villager.Profession profession = Registry.VILLAGER_PROFESSION.get(new NamespacedKey("minecraft", args[5]));
                        return kingdom.addCitizen(name, variant, profession);
                    }
                    case "district" -> {
                        if (args.length < 5) return Component.text("You must specify the details of the new district: add district <id> <item icon>");
                        String districtID = args[3];
                        Material icon = Material.getMaterial(args[4]);
                        if (icon == null) return Component.text("Item called \"" + args[4] + "\" doesnt exits", NamedTextColor.RED);
                        return kingdom.addDistrict(districtID, icon);
                    }
                    default -> {
                        return Component.text("Invalid argument. Syntaks: add [citizen | district] <properties>", NamedTextColor.RED);
                    }
                }
            }
            case "remove" -> {
                if (args.length == 2) return Component.text("You must specify what to remove: [citizen | district]", NamedTextColor.RED);
                String feature = args[2];
                switch (feature) {
                    case "citizen" -> {
                        if (args.length == 3) return Component.text("You must specify which citizen to remove: citizen <name>");
                        String name = args[3];
                        return kingdom.removeCitizen(name);
                    }
                    case "district" -> {
                        if (args.length == 3) return Component.text("Yuo must specify which district to remove: district <id>");
                        String districtID = args[3];
                        return kingdom.removeDistrict(districtID);
                    }
                    default -> {
                        return Component.text("Invalid argument. Syntaks: remove [citizen | district]", NamedTextColor.RED);
                    }
                }
            }
            case "help" -> {
                return Component.text(command.getDescription());
            }
            default -> {
                return Component.text("Invalid operator! Must be [info | set | add | remove]", NamedTextColor.RED);
            }
        }
    }
}
