package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KingdomsTabCompletion implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 1) return List.of("info", "add", "remove");
        String oper = args[0];
        switch (oper) {
            case "add", "remove" -> {
                return KingdomsManager.getKingdomIDs().stream().toList();
            }
            case "select" -> {
                if (args.length == 2) return KingdomsManager.getKingdomIDs().stream().toList();

                String kingdomID = args[1];
                Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
                if (kingdom == null) return List.of(ChatColor.RED + "Kingdom \"" + kingdomID + " doesnt exist!");
                if (args.length == 3) return kingdom.getDistrictIDs().stream().toList();

                String districtID = args[2];
                District district = kingdom.getDistrict(districtID);
                if (district == null) return List.of(ChatColor.RED + "District \"" + districtID + " doesnt exist!");
                if (args.length == 4) return district.getLocalityIDs().stream().toList();
            }
        }
        return List.of();
    }
}
