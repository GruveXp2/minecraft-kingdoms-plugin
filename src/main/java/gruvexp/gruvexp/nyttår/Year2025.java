package gruvexp.gruvexp.nyttår;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Year2025 {

    private static final List<Location> snøfnugg = new ArrayList<>(50);
    private static final List<BlockDisplay> blockDisplays = new ArrayList<>(50);
    private static final List<BlockDisplay> blockDisplays1 = new ArrayList<>(50);
    private static final List<BlockDisplay> blockDisplays2 = new ArrayList<>(50);
    private static final List<Double> radians = new ArrayList<>(50);
    private static final List<Double> length = new ArrayList<>(50);
    private static Location animationCenter;
    private static boolean isCenterRegistered = false;

    private static Location numberStart1;
    private static Location numberStart2;
    private static final List<Location> number1 = new ArrayList<>();
    private static final List<Location> number2 = new ArrayList<>();
    private static final List<Double> number1Lengths = new ArrayList<>();
    private static final List<Double> number2Lengths = new ArrayList<>();

    public static BlockDisplay testDisplay;

    public static final int NUMBER_SCALE = 2;

    public static final Location FENCEPOST_LOCATION = new Location(Main.WORLD, -4819, 70, 211);

    // TODO: man trykker på knappen og når man trykker så blir hvite concretblokka om til blockdisplay 2 stykker faktisk som smooth beveger seg mot 5 tallet for å gjør det om til 6 tall, da kommer det factorio win sfx og det vokser ut vegger rundt og sea lantern og farga glass beveger seg og nyttårsrakettan flyr opp

    public static void resetSignpost() { // clone signpost from the hidden underneath and place it above

        Block source = FENCEPOST_LOCATION.clone().add(0, -2, 0).getBlock();
        Location targetLoc = FENCEPOST_LOCATION.clone().add(0, 1, 0);

        targetLoc.getBlock().setType(Material.WHITE_CONCRETE); // a white concrete the sign will hang on. it will be used l8r to turn into the line making the 5 in 2025 to a 6
        targetLoc.add(-1, 0, 0);

        targetLoc.getBlock().setType(source.getType());
        targetLoc.getBlock().setBlockData(source.getBlockData().clone());

        if (source.getState() instanceof org.bukkit.block.Sign srcSign
                && targetLoc.getBlock().getState() instanceof org.bukkit.block.Sign tgtSign) {

            tgtSign.line(0, srcSign.line(0));
            tgtSign.line(1, srcSign.line(1));
            tgtSign.line(2, srcSign.line(2));
            tgtSign.line(3, srcSign.line(3));

            tgtSign.update();
        }
    }

    public static void transformInto2026() {
        Location spawnLoc = FENCEPOST_LOCATION.clone().add(0, 1, 0);
        spawnLoc.getBlock().setType(Material.AIR);
        for (int i = 0; i < 2; i++) {
            BlockDisplay display = (BlockDisplay) Main.WORLD.spawnEntity(spawnLoc, EntityType.BLOCK_DISPLAY);
            display.setBlock(Material.SNOW_BLOCK.createBlockData());
            display.setTeleportDuration(20);
            display.setInterpolationDuration(20);
            display.addScoreboardTag("nyttor2025");
            blockDisplays.add(display);

            final int finalI = i;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                display.teleport(numberStart2.clone().add(-2, finalI * 2, 22));
                setBlockDisplaySize(display, 2);
            }, 10L);
        }
    }

    public static void addSnøFnuggBlock(Location location) {
        snøfnugg.add(location);
    }

    public static void addNumberNode(boolean isNumber20, Location location) {
        Main.getPlugin().getLogger().info("added blocc");
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

    public static void report() {
        Bukkit.broadcast(Component.text("Displays: " + blockDisplays.size()));
        Bukkit.broadcast(Component.text("Displays1: " + blockDisplays1.size()));
        Bukkit.broadcast(Component.text("Displays2: " + blockDisplays2.size()));
        Bukkit.broadcast(Component.text("number1: " + number1.size()));
        Bukkit.broadcast(Component.text("number2: " + number2.size()));
    }

    public static void spawnCircle(Location center) {
        animationCenter = center;
        int radius = 10;
        int blocks = snøfnugg.size();
        for (int i = 0; i < blocks; i++) {
            double angle = 2*Math.PI * i/blocks;
            BlockDisplay display = (BlockDisplay) Main.WORLD.spawnEntity(center.clone().add(0, radius * Math.sin(angle), radius * Math.cos(angle)), EntityType.BLOCK_DISPLAY);
            display.setInterpolationDuration(0);
            display.setTeleportDuration(3); // sett til 25
            if (i == 0) {
                display.setBlock(Material.REDSTONE_BLOCK.createBlockData());
            } else if (i == blocks - 1) {
                display.setBlock(Material.EMERALD_BLOCK.createBlockData());
            } else {
                display.setBlock(Material.SNOW_BLOCK.createBlockData());
            }
            display.addScoreboardTag("nyttor2025");
            blockDisplays.add(display);
        }
        Main.getPlugin().getLogger().info("spawned in " + blocks + " blocks");
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
        Main.getPlugin().getLogger().info("Starting loop");
        new Teleportus(ticks).runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void rotateInwards(int ticks) {
        Main.getPlugin().getLogger().info("Starting inwards loop");
        new Vekkus(ticks).runTaskTimer(Main.getPlugin(), 0, 1);
    }

    public static void setBlockDisplaySize(BlockDisplay display, float scale) {
        setBlockDisplaySize(display, scale, scale, scale);
    }

    public static void setBlockDisplaySize(BlockDisplay display, float scaleX, float scaleY, float scaleZ) {
        // Hent eksisterende transformasjon
        Transformation currentTransform = display.getTransformation();

        // Opprett en ny skaleringsfaktor
        Vector3f scale = new Vector3f(scaleX, scaleY, scaleZ);

        // Lag en ny transformasjon med den nye skaleringsfaktoren
        Transformation newTransform = new Transformation(
                currentTransform.getTranslation(),  // Behold eksisterende posisjon
                currentTransform.getLeftRotation(), // Behold eksisterende rotasjon
                scale,                              // Angi ny skala
                currentTransform.getRightRotation() // Behold eksisterende rotasjon
        );

        // Sett den nye transformasjonen
        display.setTransformation(newTransform);
    }

    public static void animateNumber(int ticks) {
        numberStart1 = blockDisplays1.getFirst().getLocation();
        numberStart2 = blockDisplays2.getFirst().getLocation();

        blockDisplays.forEach(display -> display.setTeleportDuration(2));
        new SkiltAnimasjon(blockDisplays1, true, ticks).runTaskTimer(Main.getPlugin(), 0, 2);
        new SkiltAnimasjon(blockDisplays2, false, ticks).runTaskTimer(Main.getPlugin(), 0, 2);
    }

    private static Location getLocation(boolean isNumber20, double progress, double placement, boolean test) {

        int totalLength = (isNumber20 ? blockDisplays1.size() : blockDisplays2.size()) * NUMBER_SCALE;
        double length = totalLength * progress * placement;
        if (test) Main.getPlugin().getLogger().info(
                String.format(Locale.US,
                        "length=%.1f, (tot=%d * prog=%.2f * placement=%.1f)",
                        length, totalLength, progress, placement
                )
        );
        //Main.getPlugin().getLogger().info("getLocation(b, d, d)");
        if (isNumber20) {
            return getLocation(length, number1Lengths, number1, numberStart1, test);
        } else {
            return getLocation(length, number2Lengths, number2, numberStart2, false);
        }
    }

    @NotNull
    private static Location getLocation(double length, List<Double> numberLengths, List<Location> number, Location startLocation, boolean test) {

        String out = String.format(Locale.US,
                "getLocation(length=%.1f, numberLengths=%s, number=%s, startLocation=(%.1f, %.1f, %.1f))",
                length,
                numberLengths.stream().map(d -> String.format(Locale.US, "%.1f", d)).toList(),
                number.stream().map(l -> String.format(Locale.US, "(%.1f, %.1f, %.1f)", l.getX(), l.getY(), l.getZ())).toList(),
                startLocation.getX(), startLocation.getY(), startLocation.getZ()
        );
        if (test) Main.getPlugin().getLogger().info(out);

        int step = 0;
        while (step < numberLengths.size() - 1 && length >= numberLengths.get(step)) {
            if (test) Main.getPlugin().getLogger().info(String.format(Locale.US, "length(%.1f) >= numberLengths.get(step)(%.1f)", length, numberLengths.get(step)));
            length -= numberLengths.get(step);
            step++;
        }
        if (test) Main.getPlugin().getLogger().info(String.format(Locale.US, "length(%.1f) < numberLengths.get(step)(%.1f)", length, numberLengths.get(step)));

        if (step == numberLengths.size()) step--;
        if (test) Main.getPlugin().getLogger().info("====================");
        if (test) Main.getPlugin().getLogger().info("step: " + step);
        if (test) Main.getPlugin().getLogger().info("lengde igjen: " + length + (step==numberLengths.size() ? " (max)" : ""));

        Location fra = startLocation.clone().add(number.get(step));
        Location til = startLocation.clone().add(number.get(step + 1));
        if (test) Main.getPlugin().getLogger().info(String.format(Locale.US, "fra: (%.1f %.1f %.1f)", fra.getX(), fra.getY(), fra.getZ()));
        if (test) Main.getPlugin().getLogger().info(String.format(Locale.US, "til: (%.1f %.1f %.1f)", til.getX(), til.getY(), til.getZ()));

        Location diff = number.get(step + 1).clone().subtract(number.get(step));
        double diffI = number.get(step + 1).distance(number.get(step));
        double t = length/diffI;
        if (test) Main.getPlugin().getLogger().info(String.format(Locale.US, "t: %.2f (%.1f / %.1f)", t, length, diffI));
        Location te = diff.clone().multiply(t);
        if (test) Main.getPlugin().getLogger().info("legg til: " + String.format(Locale.US, "(%.1f %.1f %.1f)", te.getX(), te.getY(), te.getZ()));

        Location result = startLocation.clone()
                .add(number.get(step))
                .add(diff.multiply(t));
        if (test) Main.getPlugin().getLogger().info("resultat: " + String.format(Locale.US, "(%.1f %.1f %.1f)", result.getX(), result.getY(), result.getZ()));
        return result;
    }

    public static void deleteCircle() {
        for (BlockDisplay display : blockDisplays) {
            display.remove();
        }
        blockDisplays.clear();
        blockDisplays1.clear();
        blockDisplays2.clear();

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
        processFile(inputSnøfnugg, snøfnugg, false);
        String inputNumber1 = Utils.loadTxt("number1");
        processFile(inputNumber1, number1, true);
        for (int i = 1; i < number1.size(); i++) {
            double dist = number1.get(i).distance(number1.get(i - 1));
            number1Lengths.add(dist);
        }
        String inputNumber2 = Utils.loadTxt("number2");
        processFile(inputNumber2, number2, true);
        for (int i = 1; i < number2.size(); i++) {
            double dist = number2.get(i).distance(number2.get(i - 1));
            number2Lengths.add(dist);
        }
    }

    private static void processFile(String inputRaw, List<Location> number, boolean big) {
        String[] coords = inputRaw.split("\n");
        double multiply = big ? NUMBER_SCALE : 1;
        for (String coord : coords) {
            String[] loc = coord.split(",");
            number.add(new Location(Main.WORLD, Double.parseDouble(loc[0]) * multiply, Double.parseDouble(loc[1]) * multiply, Double.parseDouble(loc[2]) * multiply));
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
            Main.getPlugin().getLogger().info("Step: " + currentStep);
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
        final double TARGET_ANGLE = Math.atan((double) 4 / 3);

        private Vekkus(int steps) {
            this.totalSteps = steps;
        }

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
                setBlockDisplaySize(blockDisplays.get(i), size);
            }

            if (currentStep == totalSteps) {
                this.cancel();
            }
        }

        private double getNewAngle(int i, double progress, BlockDisplay display) {
            double targetAngle = TARGET_ANGLE;
            double startAngle = 2 * Math.PI * ((double) i / blockDisplays.size());
            if (startAngle > targetAngle && startAngle < Math.PI + targetAngle) {
                targetAngle += Math.PI;
            } else if (startAngle > targetAngle + Math.PI) {
                startAngle -= 2 * Math.PI;
            }
            if (currentStep == 1) {
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

    private static class SkiltAnimasjon extends BukkitRunnable {

        private final List<BlockDisplay> displays;
        private final boolean isNumber20;
        private final int totalSteps;
        private int step = 0;
        final int totalDisplays;

        private SkiltAnimasjon(List<BlockDisplay> displays, boolean isNumber20, int totalSteps) {
            this.displays = displays;
            this.isNumber20 = isNumber20;
            this.totalSteps = totalSteps;
            totalDisplays = displays.size();
        }

        @Override
        public void run() {
            step++;
            double progress = (double) step / totalSteps;
            for (int i = 0; i < totalDisplays; i++) {
                double placement = (double) i / totalDisplays;
                boolean test = i == totalDisplays - 1;

                displays.get(i).teleport(getLocation(isNumber20, progress, placement, test));
                //displays.get(i).teleport(getLocation(isNumber20, progress, placement));
            }
            if (step == totalSteps) cancel();
        }
    }
}
