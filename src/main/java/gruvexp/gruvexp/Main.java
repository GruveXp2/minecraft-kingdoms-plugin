package gruvexp.gruvexp;

import gruvexp.gruvexp.clock.ClockManager;
import gruvexp.gruvexp.clock.Digit;
import gruvexp.gruvexp.commands.*;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.listeners.BlockListener;
import gruvexp.gruvexp.menu.MenuListener;
import gruvexp.gruvexp.nyttår.NyttårCommand;
import gruvexp.gruvexp.nyttår.NyttårTabCompletion;
import gruvexp.gruvexp.nyttår.Year2025;
import gruvexp.gruvexp.rail.RailCartListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class Main extends JavaPlugin {

    private static Main plugin;
    public static World WORLD;
    public static World WORLD_NETHER;
    private static final int PORT = 25566; // Port used to communicate with the discord bot

    public static boolean spamPackets = false;

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new MenuListener(), this);
        getServer().getPluginManager().registerEvents(new RailCartListener(), this);
        getCommand("kingdoms").setExecutor(new KingdomsCommand());
        getCommand("kingdoms").setTabCompleter(new KingdomsTabCompletion());
        getCommand("kingdom").setExecutor(new KingdomCommand());
        getCommand("kingdom").setTabCompleter(new KingdomTabCompletion());
        getCommand("citizen").setExecutor(new CitizenCommand());
        getCommand("citizen").setTabCompleter(new CitizenTabCompletion());
        getCommand("district").setExecutor(new DistrictCommand());
        getCommand("district").setTabCompleter(new DistrictTabCompletion());
        getCommand("rail").setExecutor(new RailCommand());
        getCommand("rail").setTabCompleter(new RailTabCompletion());
        getCommand("path").setExecutor(new PathCommand());
        getCommand("path").setTabCompleter(new PathTabCompletion());
        getCommand("home").setExecutor(new HomeCommand());
        getCommand("house").setExecutor(new HouseCommand());
        getCommand("house").setTabCompleter(new HouseTabCompletion());
        getCommand("test").setExecutor(new TestCommand());
        getCommand("test").setTabCompleter(new TestTabCompletion());
        getCommand("digitalclock").setExecutor(new ClockCommand());
        getCommand("bigthing").setExecutor(new BigThingCommand());
        getCommand("shrink").setExecutor(new ShrinkCommand());
        getCommand("shrink").setTabCompleter(new ShrinkTabCompletion());
        getCommand("loadcitizens").setExecutor(new LoadCitizensCommand());
        getCommand("nyttår").setExecutor(new NyttårCommand());
        getCommand("nyttår").setTabCompleter(new NyttårTabCompletion());
        getServer().getPluginManager().registerEvents(new BlockListener(), this);
        plugin = this;
        WORLD = Bukkit.getWorld(FilePath.SERVER_NAME);
        WORLD_NETHER = Bukkit.getWorld(FilePath.SERVER_NAME + "_nether");
        HomeManager.loadData();
        KingdomsManager.loadData();
        Digit.init();
        KingdomsManager.init();
        Year2025.loadData();
        new Thread(this::startSocketServer).start(); // Start the server in a new thread to avoid blocking the main thread
    }

    @Override
    public void onDisable() {
        ClockManager.deleteNumbers();
        getLogger().info("Saving data...");
        KingdomsManager.saveData();
        getLogger().info("Disabling Kingdoms plugin...");
    }

    public static Main getPlugin() {
        return plugin;
    }

    private void startSocketServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            getLogger().info("Server listening on port " + PORT);

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

                    String command = in.readLine();
                    if (command == null || command.trim().isEmpty()) return;
                    if (command.startsWith("@")) {
                        if (command.equals("@ping")) {
                            out.write("Kingdoms: " + Bukkit.getOnlinePlayers().size() + " online");
                            out.newLine();
                            out.flush();
                        }
                    } else { // a minecraft command
                        CountDownLatch latch = new CountDownLatch(1);
                        Bukkit.getScheduler().runTask(this, () -> { // Schedule the command execution on the main thread
                            try {
                                // Execute the command on the server console
                                ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
                                String result = executeCommand(console, command);

                                synchronized (out) { // Ensure safe access to the BufferedWriter
                                    try {
                                        // Send the result back to the client
                                        out.write(result);
                                        out.newLine();
                                        out.flush();
                                        //getLogger().info("The result of the command is: \n" + result + "\n======");
                                    } catch (IOException e) {
                                        getLogger().severe("Error sending result to client: " + e.getMessage());
                                    }
                                }
                            } finally {
                                latch.countDown(); // Signal that the task is complete
                            }
                        });

                        // Wait for the task to complete before closing the resources
                        try {
                            latch.await(1, TimeUnit.SECONDS); // if the server lags so much it takes over a second to run the command, then it will quit waiting
                        } catch (InterruptedException e) {
                            getLogger().severe("Waiting for task completion interrupted: " + e.getMessage());
                        }
                    }
                    //getLogger().warning("The socket will close now");
                } catch (IOException e) {
                    getLogger().severe("Error handling client: " + e.getMessage()); // random comment is being typed for no reason, idk whartugifukg
                }
            }
        } catch (IOException e) {
            getLogger().severe("Could not listen on port " + PORT);
            e.printStackTrace();
        }
    }

    private String executeCommand(ConsoleCommandSender console, String command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            // Redirect system output to capture command output
            System.setOut(new PrintStream(baos));

            // Execute the command
            Bukkit.dispatchCommand(console, command);

            // Restore original system output
            System.setOut(originalOut);

            // Return the captured output
            return baos.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error capturing command output: " + e.getMessage();
        }
    }
}
