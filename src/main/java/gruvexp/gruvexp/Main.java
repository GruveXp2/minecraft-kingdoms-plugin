package gruvexp.gruvexp;

import gruvexp.gruvexp.clock.ClockManager;
import gruvexp.gruvexp.clock.Digit;
import gruvexp.gruvexp.commands.*;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.menu.MenuListener;
import gruvexp.gruvexp.rail.RailCartListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main plugin;
    public static World WORLD;

    public static boolean spamPackets = false;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Bukkit.getLogger().info("[Kingdoms] v20.04.24");
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new RailCartListener(), this);
        getCommand("path").setExecutor(new PathCommand());
        getCommand("path").setTabCompleter(new PathTabCompletion());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("house").setExecutor(new HouseCommand());
        getCommand("house").setTabCompleter(new HouseTabCompletion());
        getCommand("test").setExecutor(new TestCommand());
        getCommand("test").setTabCompleter(new TestTabCompletion());
        getCommand("digitalclock").setExecutor(new ClockCommand());
        getCommand("rail").setExecutor(new RailCommand());
        getCommand("rail").setTabCompleter(new RailTabCompletion());
        getCommand("bigthing").setExecutor(new BigThingCommand());
        getCommand("shrink").setExecutor(new ShrinkCommand());
        getCommand("shrink").setTabCompleter(new ShrinkTabCompletion());
        getCommand("citizen").setExecutor(new CitizenCommand());
        getCommand("citizen").setTabCompleter(new CitizenTabCompletion());
        getCommand("loadcitizens").setExecutor(new LoadCitizensCommand());
        plugin = this;
        WORLD = Bukkit.getWorld("Four Kingdoms");
        HomeManager.loadData();
        KingdomsManager.loadData();
        Digit.init();
        KingdomsManager.init();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        ClockManager.deleteNumbers();
        Bukkit.getLogger().info("Saving data...");
        KingdomsManager.saveData();
        Bukkit.getLogger().info("Disabling Kingdoms plugin...");
    }

    public static Main getPlugin() {
        return plugin;
    }
}
