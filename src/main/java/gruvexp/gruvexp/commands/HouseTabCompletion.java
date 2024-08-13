package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.Utils;
import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HouseTabCompletion implements TabCompleter {
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player p = (Player) sender;

        if (args.length == 1) {
            return new ArrayList<>(KingdomsManager.getKingdomIDs());
        }
        try {
            String kingdomID = args[0];
            Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
            if (args.length == 2) {
                return new ArrayList<>(kingdom.getDistrictIDs());
            }
            String districtID = args[1];
            District district = kingdom.getDistrict(districtID);
            if (args.length == 3) {
                return new ArrayList<>(district.getAddressIDs());
            }
            String addressID = args[2];
            Address address = district.getAddress(addressID);
            if (args.length == 4) {
                return List.of("add", "get", "list", "set", "remove");
            }
            String oper = args[3];
            if (args.length == 5) {
                switch (oper) {
                    case "get":
                    case "set":
                    case "remove":
                        return address.getHouseIDs().stream().map(Object::toString).collect(Collectors.toList());
                }
            }
            if (oper.equals("set")) {
                if (args.length == 6) {
                    return List.of("door_pos", "bed_pos", "exit_path");
                }
                String property = args[5];
                if (property.equals("door_pos") || property.equals("bed_pos")) {
                    return List.of(Utils.getTargetBlock(p, 10).toString());
                } else if (property.equals("exit_path")) {
                    return new ArrayList<>(address.getPathIDs());
                }
            }
        } catch (IllegalArgumentException e) {
            return List.of(e.getMessage());
        }
        return new ArrayList<>(0);
    }
}
