package gruvexp.gruvexp.nyttår;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class Year2025 {

    private static List<Location> snøfnugg = new ArrayList<>(50);
    private static List<BlockDisplay> blockDisplays = new ArrayList<>(50);
    private static List<BlockDisplay> blockDisplays1 = new ArrayList<>(50);
    private static List<BlockDisplay> blockDisplays2 = new ArrayList<>(50);
    private static List<Double> radians = new ArrayList<>(50);
    private static List<Double> length = new ArrayList<>(50);
    private static Location animationCenter;
    private static boolean isCenterRegistered = false;

    private static Location numberStart1;
    private static Location numberStart2;
    private static List<Location> number1 = new ArrayList<>();
    private static List<Location> number2 = new ArrayList<>();

    public static BlockDisplay testDisplay;

    public static void addSnøFnuggBlock(Location location) {
        snøfnugg.add(location);
    }

    public static void addNumberNode(boolean isNumber20, Location location) {
        if (isNumber20) {
            number1.add(location);
        } else {
            number2.add(location);
        }
    }

    public static void registerCenter(Location centerLocation) {
        if (!isCenterRegistered) {
            return;
        }
        snøfnugg.forEach(loc -> loc.subtract(centerLocation));
        isCenterRegistered = true;
    }

    public static void registerNumberStart(boolean isNumber20, Location startLocation) {
        if (isNumber20) {
            numberStart1 = startLocation;
            number1.forEach(loc -> loc.subtract(numberStart1).multiply(2));
        } else {
            numberStart2 = startLocation;
            number2.forEach(loc -> loc.subtract(numberStart2).multiply(2));
        }
    }

    public static void spawnCircle(Location center) {
        animationCenter = center;
        int radius = 10;
        int blocks = snøfnugg.size();
        for (int i = 0; i < blocks; i++) {
            double angle = 2*Math.PI * i/blocks;
            BlockDisplay display = (BlockDisplay) Main.WORLD.spawnEntity(center.clone().add(0, radius * Math.sin(angle), radius * Math.cos(angle)), EntityType.BLOCK_DISPLAY);
            display.setTeleportDuration(25);
            if (i == 0) {
                display.setBlock(Material.REDSTONE_BLOCK.createBlockData());
            } else if (i == 10) {
                display.setBlock(Material.LAPIS_BLOCK.createBlockData());
            } else {
                display.setBlock(Material.SNOW_BLOCK.createBlockData());
            }
            display.addScoreboardTag("nyttor2025");
            blockDisplays.add(display);
        }
        Bukkit.getLogger().info("spawned in " + blocks + " blocks");
    }

    public static void snowFlake() {
        for (int i = 0; i < snøfnugg.size(); i++) {
            blockDisplays.get(i).teleport(animationCenter.clone().add(snøfnugg.get(i)));
        }
    }

    public static void rotatingCircle(int ticks) {
        for (BlockDisplay display : blockDisplays) {
            Location relativeLoc = display.getLocation().subtract(animationCenter); // 0.1: 1/radius
            radians.add(Math.atan2(relativeLoc.getY(), relativeLoc.getZ()));
            length.add(Math.hypot(relativeLoc.getY(), relativeLoc.getZ()));
            display.setTeleportDuration(1);
        }
        Bukkit.getLogger().info("Starting loop");
        new Teleportus(ticks).runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void rotateInwards(int ticks) {
        Bukkit.getLogger().info("Starting inwards loop");
        new Vekkus(ticks).runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void setBlockDisplaySize(BlockDisplay display, float scaleX, float scaleY, float scaleZ) {
        // Hent eksisterende transformasjon
        Transformation currentTransform = display.getTransformation();

        // Opprett en ny skaleringsfaktor
        Vector3f scale = new Vector3f(scaleX, scaleY, scaleZ);

        // Lag en ny transformasjon med den nye skaleringsfaktoren
        Transformation newTransform = new Transformation(
                currentTransform.getTranslation(),   // Behold eksisterende posisjon
                currentTransform.getLeftRotation(), // Behold eksisterende rotasjon
                scale,                              // Angi ny skala
                currentTransform.getRightRotation() // Behold eksisterende rotasjon
        );

        // Sett den nye transformasjonen
        display.setTransformation(newTransform);
    }

    public static void animateNumber() {

    }

    private static void getLocation(double progress) {

    }

    public static void deleteCircle() {
        for (BlockDisplay display : blockDisplays) {
            display.remove();
        }
        blockDisplays.clear();
        length.clear();
        radians.clear();
    }

    public static void saveData() {
        /*StringBuilder output = new StringBuilder();
        for (Location loc : snøfnugg) {
            output.append(loc.getX()).append(",").append(loc.getY()).append(",").append(loc.getZ()).append("\n");
        }
        Utils.saveTxt("snøfnugg", output.toString());*/
        StringBuilder output1 = new StringBuilder();
        for (Location loc : number1) {
            output1.append(loc.getX()).append(",").append(loc.getY()).append(",").append(loc.getZ()).append("\n");
        }
        Utils.saveTxt("number1", output1.toString());
        StringBuilder output2 = new StringBuilder();
        for (Location loc : number2) {
            output2.append(loc.getX()).append(",").append(loc.getY()).append(",").append(loc.getZ()).append("\n");
        }
        Utils.saveTxt("number2", output2.toString());
    }

    public static void loadData() {
        String inputSnøfnugg = Utils.loadTxt("snøfnugg");
        processFile(inputSnøfnugg, snøfnugg);
        String inputNumber1 = Utils.loadTxt("number1");
        processFile(inputNumber1, number1);
        String inputNumber2 = Utils.loadTxt("number2");
        processFile(inputNumber2, number2);
    }

    private static void processFile(String inputNumber1, List<Location> number1) {
        String[] coords1 = inputNumber1.split("\n");
        for (int i = 0; i < coords1.length - 1; i++) {
            String[] loc = coords1[i].split(",");
            number1.add(new Location(Main.WORLD, Double.parseDouble(loc[0]), Double.parseDouble(loc[1]), Double.parseDouble(loc[2])));
        }
    }

    public static void test1(Player p) {
        testDisplay = (BlockDisplay) Main.WORLD.spawnEntity(p.getLocation(), EntityType.BLOCK_DISPLAY);
        testDisplay.setBlock(Material.COPPER_BULB.createBlockData());
        testDisplay.addScoreboardTag("testdisplay");
    }

    public static void test2() {
        testDisplay.setInterpolationDelay(10);
    }

    public static void test3() {
        testDisplay.setInterpolationDuration(50);
    }

    public static void test4() {
        testDisplay.teleport(testDisplay.getLocation().add(0, 2, 0));
    }

    private static class Teleportus extends BukkitRunnable {

        final int totalSteps;
        int currentStep = 0;

        private Teleportus(int steps) {
            this.totalSteps = steps;
        }

        @Override
        public void run() {
            Bukkit.getLogger().info("Step: " + currentStep);
            currentStep++;
            double progress = (double) currentStep / totalSteps;

            for (int i = 0; i < blockDisplays.size(); i++) {
                double originalAngle = 2*Math.PI*((double) i / blockDisplays.size());
                double startAngle = radians.get(i);
                if (originalAngle > Math.PI && Math.abs(startAngle - Math.PI) > 0.01) originalAngle -= 2 * Math.PI;
                double diffAngle = originalAngle - startAngle;
                double newAngle = startAngle + (diffAngle - 2*Math.PI) * progress;

                double startDist = length.get(i);
                double diffDist = 10 - startDist;
                double newDist = startDist + diffDist * progress;

                blockDisplays.get(i).teleport(animationCenter.clone().add(0, newDist * Math.sin(newAngle), newDist * Math.cos(newAngle)));
            }

            if (currentStep == totalSteps) {
                this.cancel();
            }
        }
    }

    private static class Vekkus extends BukkitRunnable {

        final int totalSteps;
        int currentStep = 0;

        private Vekkus(int steps) {
            this.totalSteps = steps;
        }
        double targetAngle = Math.atan((double) 4 / 3);

        @Override
        public void run() {
            //Bukkit.getLogger().info("Step: " + currentStep);
            currentStep++;
            double progress = (double) currentStep / totalSteps;

            double startDist = 10;
            double diffDist = Math.hypot(4, 3) - startDist;
            for (int i = 0; i < blockDisplays.size(); i++) {
                double newAngle = getNewAngle(i, progress, blockDisplays.get(i));

                double newDist = startDist + diffDist * progress;

                float size = (float) (1 + progress);

                blockDisplays.get(i).teleport(animationCenter.clone().add(0, newDist * Math.sin(newAngle), newDist * Math.cos(newAngle)));
                setBlockDisplaySize(blockDisplays.get(i), size, size, size);
            }

            if (currentStep == totalSteps) {
                this.cancel();
            }
        }

        private double getNewAngle(double i, double progress, BlockDisplay display) {
            double startAngle = 2 * Math.PI * (i / blockDisplays.size());
            if (startAngle > targetAngle && startAngle < Math.PI + targetAngle) {
                targetAngle += Math.PI;
            } else if (startAngle > targetAngle + Math.PI) {
                startAngle -= 2 * Math.PI;
            }
            if (i == 0) {
                if (targetAngle < Math.PI) {
                    blockDisplays1.add(display);
                } else {
                    blockDisplays2.add(display);
                }
            }
            //if (targetAngle > Math.PI && Math.abs(startAngle - Math.PI) > 0.01) targetAngle -= 2 * Math.PI;
            double diffAngle = targetAngle - startAngle;
            return startAngle + (diffAngle - 2 * Math.PI) * progress;
        }
    }
}
