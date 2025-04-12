package gruvexp.gruvexp.commands;

import gruvexp.gruvexp.core.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CitizenCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (!(sender instanceof Player p)) {return true;}
        if (args.length == 0) {return false;}
        Component result = processCommand(p, args, command);
        p.sendMessage(result);
        return true;
    }

    private Component processCommand(Player p, String[] args, Command command) {
        Kingdom kingdom = KingdomsManager.getSelectedKingdom(p);
        String citizenName = args[0];
        Citizen citizen = kingdom.getCitizen(citizenName);
        if (citizen == null) return Component.text("Citizen \"" + citizenName + "\" doesnt exist!", NamedTextColor.RED);
        if (args.length == 1) return Component.text("You must specify an operation [info | set | tp | help]");

        String oper = args[1];
        switch (oper) {
            case "info" -> {
                return Component.text("Citizen ").append(citizen.name()).append(Component.text(" has the following data:\n"))
                        .append(Component.text("Type: " + citizen.getType() + " " + citizen.getProfession()))
                        .append(Component.text("Home address: ")).append(citizen.homeAddress())
                        .append(Component.text("Work address: ")).append(citizen.workAddress())
                        .append(Component.text("Bio: ")).append(citizen.bio());
            }
            case "set" -> {
                if (args.length < 4) return Component.text("You must specify what to set and a value: set [profession | home_addr | work_addr] <value>", NamedTextColor.RED);
                String property = args[2];
                switch (property) {
                    case "profession" -> {
                        Villager.Profession profession = Registry.VILLAGER_PROFESSION.get(new NamespacedKey("minecraft", args[3]));
                        if (profession == null) return Component.text("\"" + args[3] + "\" is not a valid profvession!", NamedTextColor.RED);
                        return citizen.setProfession(profession);
                    }
                    case "home_address" -> {
                        String homeAddress = args[3];
                        String[] homeAddressParts = homeAddress.split(":");
                        District district = kingdom.getDistrict(homeAddressParts[0]);
                        if (district == null) return Component.text("District \"" + homeAddressParts[0] + "\" doesnt exist!", NamedTextColor.RED);
                        Locality locality = district.getLocality(homeAddressParts[1]);
                        if (locality == null) return Component.text("Locality \"" + homeAddressParts[1] + "\" doesnt exist!", NamedTextColor.RED);
                        House house = locality.getHouse(Integer.parseInt(homeAddressParts[1]));
                        if (house == null) return Component.text("House \"" + homeAddressParts[1] + " " + homeAddressParts[2] + "\" doesnt exist!", NamedTextColor.RED);
                        House oldHouse = citizen.getHome();
                        if (oldHouse != house) oldHouse.removeResident(citizen);
                        house.addResident(citizen);
                        return citizen.setHome(house);
                    }
                    case "bio" -> {
                        String bio = String.join(" ", Arrays.copyOfRange(args, 4, args.length));
                        return citizen.setBio(bio);
                    }
                    case "name", "type" -> {
                        return Component.text("These properties cant be changed, they are part of their identity", NamedTextColor.RED);
                    }
                    default -> {
                        return Component.text("\"" + property + "\" is not a property of a citizen!", NamedTextColor.RED);
                    }
                }
            }
            case "tp" -> {
                p.teleport(kingdom.getCitizen(citizenName).getVillager());
                return Component.text("Successfully tped");
            }
            case "help" -> {
                return Component.text(command.getDescription());
            }
            default -> {
                return Component.text("Invalid operation! Must be [info | set | remove | tp | help]", NamedTextColor.RED);
            }
        }
    }
}
