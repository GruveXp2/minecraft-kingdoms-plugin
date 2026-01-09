package gruvexp.gruvexp.nyttår;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.rail.Coord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NyttårCommand implements CommandExecutor {

    public static boolean settingBlocks = false;
    public static boolean settingNumberBlocks = false;
    public static boolean isSetting20 = true;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "you need 1 or more arguments");
            return true;
        }

        String oper = args[0];
        Player david = Bukkit.getPlayer("GruveXp");
        switch (oper) {
            case "reset_signpost" -> Year2025.resetSignpost();
            case "fix_year" -> Year2025.transformInto2026();
            case "lag_tekst" -> {
                assert david != null;
                david.chat("/function forskerlinja:god_jul_forskerlinja");
                Forskerlinja.location = david.getLocation();
                Forskerlinja.makeBlocks(david);
            }
            case "register_center" -> {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Ta med koordinater");
                    return true;
                }
                Coord koordinat = new Coord(args[1], args[2], args[3]);
                Location centerLocation = koordinat.toLocation(Main.WORLD);
                Year2025.registerCenter(centerLocation);
                sender.sendMessage("Sentrum ble registrert");
            }
            case "register_number_center" -> {
                if (args.length < 5) {
                    sender.sendMessage(ChatColor.RED + "Ta med tall og koordinater");
                    return true;
                }
                String number = args[1];
                boolean isNumber20 = (number.equals("1"));
                Coord koordinat = new Coord(args[2], args[3], args[4]);
                Location centerLocation = koordinat.toLocation(Main.WORLD);
                Year2025.registerNumberStart(isNumber20, centerLocation);
                sender.sendMessage("Start ble registrert for " + number);
            }
            case "toggle_set_blocks" -> {
                if (args.length == 2) {
                    settingNumberBlocks = !settingNumberBlocks;
                    String number = args[1];
                    isSetting20 = number.equals("1");
                    sender.sendMessage("Setting number blocks: " + settingNumberBlocks + ", type: " + number);
                } else {
                    settingBlocks = !settingBlocks;
                    sender.sendMessage("setting blocks ble satt til " + settingBlocks);
                }
            }
            case "spawn_circle" -> {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Ta med koordinater");
                    return true;
                }
                Coord koordinat = new Coord(args[1], args[2], args[3]);
                Location centerLocation = koordinat.toLocation(Main.WORLD).add(-0.5, 0, -0.5);
                Year2025.spawnCircle(centerLocation);
                sender.sendMessage("Spawner sirkel");
            }
            case "spawn_outline" -> {
                Year2025.spawnOutline();
                Year2025.animateOutlineExpansion();
            }
            case "animate_popout" -> Year2025.animatePopout();
            case "report" -> Year2025.report();
            case "run_snowflake" -> {
                Year2025.snowFlake();
                sender.sendMessage("Gjør om til snøfnugg");
            }
            case "run_glass" -> Year2025.runGlassAnimation();
            case "start_light" -> Year2025.startLightAnimation();
            case "start_phase_1" -> {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "You must specify xyz of the center of the animation");
                    return true;
                }
                Coord koordinat = new Coord(args[1], args[2], args[3]);
                Location centerLocation = koordinat.toLocation(Main.WORLD).add(-0.5, 0, -0.5);
                Year2025.auto = true;
                Year2025.startPhase1(centerLocation);
            }
            case "rotate_snowflake" -> {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Ta med antall ticks");
                    return true;
                }
                int ticks = Integer.parseInt(args[1]);
                Year2025.rotatingCircle(ticks);
                sender.sendMessage("Roterer snøfnugg tilbake til sirkel");
            }
            case "teleportus_vekkus" -> {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Ta med antall ticks");
                    return true;
                }
                int ticks = Integer.parseInt(args[1]);
                Year2025.rotateInwards(ticks);
            }
            case "animate_number" -> {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Ta med antall ticks");
                    return true;
                }
                int ticks = Integer.parseInt(args[1]);
                Year2025.animateNumber(ticks);
                sender.sendMessage("Animerer tall");
            }
            case "delete_circle" -> {
                Year2025.deleteCircle();
                sender.sendMessage("Fjerner bort");
            }
            case "save_positions" -> {
                Year2025.saveData();
                sender.sendMessage("Lagra data");
            }
            case "load_positions" -> {
                Year2025.loadData();
                sender.sendMessage("Lasta inn data");
            }
            case "test" -> {
                if (args.length == 1) {
                    sender.sendMessage(ChatColor.RED + "Ta med tall");
                    return true;
                }
                String tall = args[1];
                switch (tall) {
                    case "1" -> Year2025.test1((Player) sender);
                    case "2" -> Year2025.test2();
                    case "3" -> Year2025.test3();
                    case "4" -> Year2025.test4();
                }
            }
            default -> sender.sendMessage(ChatColor.RED + oper + " fins ikke!");
        }
        return true;
    }
}
