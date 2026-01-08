package gruvexp.gruvexp.nyttår;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.block.data.type.Light;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.*;

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

    private static final Set<Location> numberLoc = new HashSet<>();
    private static final Set<BlockDisplay> outline = new HashSet<>(); // outline where the stairs etc will spawn
    private static final Set<BlockDisplay> outlineCenter = new HashSet<>(); // the blocks that outline will spawn around

    private static final Set<BlockDisplay> outlineRight = new HashSet<>();
    private static final Set<BlockDisplay> outlineLeft = new HashSet<>();
    private static final Set<BlockDisplay> outlineTop = new HashSet<>();
    private static final Set<BlockDisplay> outlineBottom = new HashSet<>();

    private static final Set<BlockDisplay> frogLights = new HashSet<>();

    private static final List<Integer> number1GlassSteps = List.of(1, 5, 9, 3, 3, 3, 4, 2);
    private static final List<Integer> number2GlassSteps = List.of(1, 5, 9, 3, 3, 3, 9, 3, 3, 3, 4, 2, 1, 2, 2);
    private static final List<Vector> number1GlassTps = List.of(
            new Vector(-2, 0, 0),
            new Vector(0, -2, 0),
            new Vector(0, 0, -2),
            new Vector(0, 2, 0),
            new Vector(0, 0, 2),
            new Vector(0, 2, 0),
            new Vector(0, 0, -2),
            new Vector(2, 0, 0)
    );
    private static final List<Vector> number2GlassTps = List.of(
            new Vector(-2, 0, 0),
            new Vector(0, 2, 0),
            new Vector(0, 0, 2),
            new Vector(0, -2, 0),
            new Vector(0, 0, -2),
            new Vector(0, -2, 0),
            new Vector(0, 0, 2),
            new Vector(0, 2, 0),
            new Vector(0, 0, -2),
            new Vector(0, 2, 0),
            new Vector(0, 0, 2),
            new Vector(2, 0, 0),
            new Vector(-2, 0, 0),
            new Vector(0, 2, 0),
            new Vector(2, 0, 0)
    );
    private static GlassAnimation glassAni1;
    private static GlassAnimation glassAni2;

    private static boolean stop = false;

    private static class GlassAnimation {
        private static final List<Material> glassColors = List.of(
                Material.RED_STAINED_GLASS,
                Material.ORANGE_STAINED_GLASS,
                Material.YELLOW_STAINED_GLASS,
                Material.LIME_STAINED_GLASS,
                Material.GREEN_STAINED_GLASS,
                Material.CYAN_STAINED_GLASS,
                Material.LIGHT_BLUE_STAINED_GLASS,
                Material.BLUE_STAINED_GLASS,
                Material.PURPLE_STAINED_GLASS,
                Material.MAGENTA_STAINED_GLASS,
                Material.PINK_STAINED_GLASS
        );
        private final List<Integer> glassSteps;
        private final List<Vector> glassTps;
        private final Location startLoc;

        private final Map<BlockDisplay, Integer> currentStep = new HashMap<>();
        private final Map<BlockDisplay, Integer> currentPart = new HashMap<>();
        private final Set<BlockDisplay> glassBlocks = new HashSet<>();
        private boolean stop = false;

        public GlassAnimation(List<Integer> glassSteps, List<Vector> glassTps, Location start) {
            this.glassSteps = glassSteps;
            this.glassTps = glassTps;
            this.startLoc = start;
            for (int i = 0; i < glassSteps.stream().mapToInt(Integer::intValue).sum() + 1; i++) {
                BlockDisplay glass = spawnDisplay(start, glassColors.get(i % glassColors.size()).createBlockData());
                glass.setTeleportDuration(10);
                setBlockDisplaySize(glass, 2);
                currentStep.put(glass, -i);
                currentPart.put(glass, 0);
                glassBlocks.add(glass);
            }
        }

        public void startAnimation() {
            new BukkitRunnable() {
                public void run() {
                    if (stop) {
                        cancel();
                        stop = false;
                    }

                    for (BlockDisplay glass : glassBlocks) {
                        int step = currentStep.get(glass);
                        step++;
                        if (step <= 0) {
                            currentStep.put(glass, step);
                            continue;
                        }
                        int part = currentPart.get(glass);
                        glass.teleport(glass.getLocation().add(glassTps.get(part)));
                        if (step == glassSteps.get(part)) {
                            step = 0;
                            if (part == glassTps.size() - 1) {
                                part = -1;
                                setBlockDisplaySize(glass, 0.5f);
                                glass.teleport(startLoc);
                                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> setBlockDisplaySize(glass, 2f), 4L);
                            } else if (part == 11) {
                                setBlockDisplaySize(glass, 0.5f);
                                glass.teleport(startLoc.clone().add(0, 0, 24));
                                Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> setBlockDisplaySize(glass, 2f), 4L);
                            }
                            currentPart.put(glass, part + 1);
                        }
                        currentStep.put(glass, step);
                    }
                }
            }.runTaskTimer(Main.getPlugin(), 0, 10);
        }
    }

    public static void startLightAnimation() {
        new BukkitRunnable() {
            int t = 0;
            public void run() {
                if (stop) {
                    cancel();
                    Bukkit.getScheduler().runTaskLater(Main.getPlugin(), bukkitTask -> stop = false, 2L);
                    return;
                }
                t++;
                for (Location loc : numberLoc) {
                    int totalLight = Math.abs(Math.floorMod(loc.getBlockZ() - loc.getBlockY() - t, 40)/2 - 10) + 5;

                    Block block = loc.getBlock();
                    setLight(block, totalLight);
                    setLight(block.getRelative(BlockFace.UP), totalLight);
                    setLight(block.getRelative(BlockFace.SOUTH), totalLight);
                    setLight(block.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP), totalLight);
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);
    }

    private static void setLight(Block block, int lightlevel) {
        block.setType(Material.LIGHT);
        Light light = (Light) block.getBlockData();
        light.setLevel(lightlevel);
        block.setBlockData(light);
    }

    public static void initGlass() {
        glassAni1 = new GlassAnimation(number1GlassSteps, number1GlassTps, numberStart1.clone().add(0.5, 0, -2));
        glassAni2 = new GlassAnimation(number2GlassSteps, number2GlassTps, numberStart2.clone().add(0.5, 0, -2));
    }

    public static void runGlassAnimation() {
        glassAni1.startAnimation();
        glassAni2.startAnimation();
    }

    public static void stopGlassAnimation() {
        if (glassAni1 == null) return;
        glassAni1.stop = true;
        glassAni2.stop = true;
        stop = true;
    }

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

        if (source.getState() instanceof Sign srcSign
                && targetLoc.getBlock().getState() instanceof Sign tgtSign) {

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
            outlineCenter.add(display);


            final int finalI = i;
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                display.teleport(numberStart2.clone().add(-2, finalI * 2, 22));
                setBlockDisplaySize(display, 2);
            }, 10L);
        }

        for (int i = 1; i < 5; i++) {
            int finalI = i;
            new BukkitRunnable() {
                double t = 0;
                double Δy = (double) -finalI /(8*10) + 0.001;
                final BlockDisplay display = blockDisplays1.get(23 + finalI);
                Location loc = display.getLocation();

                public void run() {
                    t++;
                    loc.add(0, Δy, 0);
                    display.teleport(loc);

                    if (t == 20) cancel();
                }
            }.runTaskTimer(Main.getPlugin(), 0, 1);
            new BukkitRunnable() {
                double t = 0;
                double Δy = (double) -finalI /(8*10) + 0.001;
                final BlockDisplay display = blockDisplays2.get(41 + finalI);
                Location loc = display.getLocation();

                public void run() {
                    t++;
                    loc.add(0, Δy, 0.001*finalI); // add z to workaround a bug where z ends up at .99
                    display.teleport(loc);

                    if (t == 20) cancel();
                }
            }.runTaskTimer(Main.getPlugin(), 0, 1);
            Bukkit.getScheduler().runTaskLater(Main.getPlugin(), () -> {
                for (BlockDisplay display : blockDisplays) {
                    display.teleport(display.getLocation().getBlock().getLocation());
                }
            }, 21L);
        }
    }

    public static void spawnOutline() {
        for (BlockDisplay display : outlineCenter) { // legger til alle locateions
            numberLoc.add(display.getLocation().getBlock().getLocation());
        }
        BlockData green = Material.VERDANT_FROGLIGHT.createBlockData();
        BlockData purple = Material.PEARLESCENT_FROGLIGHT.createBlockData();
        BlockData yellow = Material.OCHRE_FROGLIGHT.createBlockData();
        for (Location loc : numberLoc) {
            Location spawnLoc = loc.clone();
            BlockData color = green;
            if (loc.getZ() > 192 && loc.getZ() < 203) color = yellow;
            else if (loc.getZ() > 216) color = purple;

            frogLights.add(spawnDisplay(spawnLoc.add(1, 0, 0), color));
            frogLights.add(spawnDisplay(spawnLoc.add(0, 0, 1), color));
            frogLights.add(spawnDisplay(spawnLoc.add(0, 1, 0), color));
            frogLights.add(spawnDisplay(spawnLoc.add(0, 0, -1), color));
        }

        initGlass();

        BlockData quartsData = Material.QUARTZ_BLOCK.createBlockData();
        for (Location loc : numberLoc) { // spawner inn trapper
            Location above = loc.clone().add(0, 2, 0);
            // top
            if (!numberLoc.contains(above)) {
                BlockData data = Material.QUARTZ_STAIRS.createBlockData(blockData -> {
                    ((Stairs) blockData).setFacing(BlockFace.EAST);
                    ((Stairs) blockData).setHalf(Bisected.Half.BOTTOM);
                });
                outlineTop.add(spawnDisplay(above.add(1, 0, 0), quartsData));
                outlineTop.add(spawnDisplay(above.add(0, 0, 1), quartsData));
                if (!numberLoc.contains(above.add(-1, 0, 1))) {
                    //place a stairblockk above right
                    outlineTop.add(spawnDisplay(above.clone().add(0, 0, -1), data));
                }
                if (!numberLoc.contains(above.add(0, 0, -4))) {
                    //place a stairblock above left
                    BlockData data2 = Material.QUARTZ_STAIRS.createBlockData(blockData -> { //QUARTZ_STAIRS
                        ((Stairs) blockData).setFacing(BlockFace.EAST);
                        ((Stairs) blockData).setHalf(Bisected.Half.BOTTOM);
                    });
                    outlineTop.add(spawnDisplay(above.add(0, 0, 2), data2));
                }
            }
            // bottom
            Location below = loc.clone().add(0, -2, 0);
            if (!numberLoc.contains(below)) {
                BlockData data = Material.QUARTZ_STAIRS.createBlockData(blockData -> { //QUARTZ_STAIRS
                    ((Stairs) blockData).setFacing(BlockFace.EAST);
                    ((Stairs) blockData).setHalf(Bisected.Half.TOP);
                });
                outlineBottom.add(spawnDisplay(below.add(1, 1, 0), quartsData));
                outlineBottom.add(spawnDisplay(below.add(0, 0, 1), quartsData));
                if (!numberLoc.contains(below.add(-1, -1, 1))) {
                    //place a stairblockk below right
                    outlineBottom.add(spawnDisplay(below.clone().add(0, 1, -1), data));
                }
                if (!numberLoc.contains(below.add(0, 0, -4))) {
                    BlockData data2 = Material.QUARTZ_STAIRS.createBlockData(blockData -> { //QUARTZ_STAIRS
                        ((Stairs) blockData).setFacing(BlockFace.EAST);
                        ((Stairs) blockData).setHalf(Bisected.Half.TOP);
                    });
                    //place a stairblock below left
                    outlineBottom.add(spawnDisplay(below.add(0, 1, 2), data2));
                }
            }
            // sides
            Location side = loc.clone().add(0, 0, 2);
            if (!numberLoc.contains(side)) { // right
                // plasér vanlige blokker
                Location sideClone = side.clone();
                outlineRight.add(spawnDisplay(sideClone, quartsData));
                outlineRight.add(spawnDisplay(sideClone.add(0,  1, 0), quartsData));
                outlineRight.add(spawnDisplay(sideClone.add(1,  0, 0), quartsData));
                outlineRight.add(spawnDisplay(sideClone.add(0, -1, 0), quartsData));
            }
            side.add(0, 0, -4);
            if (!numberLoc.contains(side)) { // left
                outlineLeft.add(spawnDisplay(side.add(0, 0, 1), quartsData));
                outlineLeft.add(spawnDisplay(side.add(0, 1, 0), quartsData));
                outlineLeft.add(spawnDisplay(side.add(1,  0, 0), quartsData));
                outlineLeft.add(spawnDisplay(side.add(0, -1, 0), quartsData));
            }
        }
    }

    public static void animateOutlineExpansion() {
        int TICKS = 20;

        // TOP
        outlineTop.forEach(display -> setBlockDisplaySize(display, 1, 0.05f, 1));
        new BukkitRunnable() {
            int t = 0;
            final float Δy = (float) 1 / TICKS;

            public void run() {
                t++;
                outlineTop.forEach(display -> setBlockDisplaySize(display, 1, Δy*t, 1));

                if (t == 20) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);

        // RIGHT
        outlineRight.forEach(display -> setBlockDisplaySize(display, 1, 1, 0.05f));
        new BukkitRunnable() {
            int t = 0;
            final float Δz = (float) 1 / TICKS;

            public void run() {
                t++;
                outlineRight.forEach(display -> setBlockDisplaySize(display, 1, 1, Δz *t));

                if (t == 20) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 10, 1);

        // BOTTOM
        outlineBottom.forEach(display -> {
            setBlockDisplaySize(display, 1, 0.05f, 1);
            display.teleport(display.getLocation().add(0, 1, 0));
        });
        new BukkitRunnable() {
            int t = 0;
            final float Δy = (float) 1 / TICKS;

            public void run() {
                t++;
                outlineBottom.forEach(display -> {
                    setBlockDisplaySize(display, 1, Δy * t, 1);
                    display.teleport(display.getLocation().add(0, -Δy, 0));
                });

                if (t == 20) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 20, 1);

        // LEFT
        outlineLeft.forEach(display -> {
            setBlockDisplaySize(display, 1, 1, 0.05f);
            display.teleport(display.getLocation().add(0, 0, 1));
        });
        new BukkitRunnable() {
            int t = 0;
            final float Δz = (float) 1 / TICKS;

            public void run() {
                t++;
                outlineLeft.forEach(display -> {
                    setBlockDisplaySize(display, 1, 1, Δz * t);
                    display.teleport(display.getLocation().add(0, 0, -Δz));
                });

                if (t == 20) cancel();
            }
        }.runTaskTimer(Main.getPlugin(), 30, 1);
    }

    public static void animatePopout() {
        for (BlockDisplay display : blockDisplays) {
            if (!numberLoc.contains(display.getLocation().getBlock().getLocation())) continue;

            int dist = (int) animationCenter.distance(display.getLocation());
            new BukkitRunnable() {
                int t = 0;
                final Location startLoc = display.getLocation();

                public void run() {
                    t++;
                    display.teleport(startLoc.clone().add(-2*Math.sin((double) t /20 * Math.PI), 0, 0));

                    if (t == 30) {
                        cancel();
                        setBlockDisplaySize(display, 0);
                    }
                }
            }.runTaskTimer(Main.getPlugin(), dist, 1);
        }
    }

    public static BlockDisplay spawnDisplay(Location loc, BlockData data) {
        if (loc == null || loc.getWorld() == null || data == null) throw new IllegalArgumentException("null in arguments!");

        BlockDisplay display = loc.getWorld().spawn(loc, BlockDisplay.class);

        display.setBlock(data);
        display.addScoreboardTag("nyttor2025");
        outline.add(display);
        return display;
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
        outlineCenter.addAll(blockDisplays);

        hideDisplay(blockDisplays1.get(9));
        for (int i = 28; i < 34; i++) {
            hideDisplay(blockDisplays1.get(i));
        }
        for (int i = 34; i < blockDisplays1.size(); i++) {
            outlineCenter.remove(blockDisplays1.get(i));
        }

        hideDisplay(blockDisplays2.get(9));
        hideDisplay(blockDisplays2.get(28));
        for (int i = 46; i < 52; i++) {
            hideDisplay(blockDisplays2.get(i));
        }
        for (int i = 52; i < blockDisplays2.size(); i++) {
            outlineCenter.remove(blockDisplays2.get(i));
        }

        blockDisplays.forEach(display -> display.setTeleportDuration(2));
        new SkiltAnimasjon(blockDisplays1, true, ticks).runTaskTimer(Main.getPlugin(), 0, 2);
        new SkiltAnimasjon(blockDisplays2, false, ticks).runTaskTimer(Main.getPlugin(), 0, 2);
    }

    private static void hideDisplay(BlockDisplay display) {
        setBlockDisplaySize(display, 0);
        outlineCenter.remove(display);
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
        outline.clear();
        outlineCenter.clear();
        outlineTop.clear();
        outlineRight.clear();
        outlineBottom.clear();
        outlineLeft.clear();
        frogLights.clear();

        length.clear();
        radians.clear();

        stopGlassAnimation();
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
                boolean test = /*i == totalDisplays - 1;*/ false;

                displays.get(i).teleport(getLocation(isNumber20, progress, placement, test));
                //displays.get(i).teleport(getLocation(isNumber20, progress, placement));
            }
            if (step == totalSteps) cancel();
        }
    }
}
