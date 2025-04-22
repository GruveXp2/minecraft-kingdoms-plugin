package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.Locality;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.path.WalkPath;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.RideableMinecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DriveCart extends BukkitRunnable {

    public static float TURN_OFFSET = (float) (1 - Math.sqrt(2) / 2);

    int sectionLength;
    int totalDistance = 0;
    int counter = 0;
    char direction; // hvilken retning carten kjører. når man kommer til svinger vil den nye retninga avhengig av den forrige retninga
    final Minecart cart;
    Section currentSection;
    final Location loc;
    District currentDistrict;
    final Locality targetLocality;
    String targetHouseNr;
    final Entity passenger;
    final int[] Δpos = new int[3]; // ΔPos om man kjører treigt blir Δpos huska.
    float speed; // m/t
    int nextSpeedChange;
    float speedOffset = 0; // svinger
    float moves = 0; // hvor mange moves som skal gjøres på veien

    // DEBUG
    final Player gruveXp = Bukkit.getPlayer("GruveXp");

    public DriveCart(@NotNull Minecart cart, Entity passenger, Section startSection, char direction, Locality targetLocality) {
        this.targetLocality = targetLocality;
        this.cart = cart;
        loc = cart.getLocation();
        this.direction = direction;
        currentSection = startSection;
        this.currentDistrict = startSection.getDistrict();
        sectionLength = currentSection.getLength();
        if (currentSection.getSpeed(0) == null) terminate("Sector " + currentSection.id + " starting speed is not set!");
        speed = currentSection.getSpeed(0) / 2f;
        nextSpeedChange = currentSection.getNextSpeedIndex(1);
        cart.setMaxSpeed(speed);
        updateVelocity();
        cart.addScoreboardTag("running");
        this.passenger = passenger;

        if (sectionLength == 0) terminate("Sector " + currentSection.id + " length is not calculated!");
    }

    public DriveCart(Villager villager, Entrypoint entrypoint, Locality targetLocality) { //brukes av path systemet
        this((Minecart) Main.WORLD.spawnEntity(entrypoint.getCoord().toLocation(Main.WORLD), EntityType.MINECART), villager, entrypoint.getSection(), entrypoint.getDirection(), targetLocality);
        villager.teleport(cart);
        cart.addPassenger(villager); // villageren setter seg i minecarten
        if (cart.getPassengers().getFirst() instanceof Villager) {
            //Bukkit.broadcastMessage("a villager sits in the cart");
        } else {
            Bukkit.broadcast(Component.text(".addPassenger() is trash and dont work"));
        }
        //Bukkit.broadcastMessage("as you clearly see, the villager is now sitting in the minecart");
    }

    public void terminate(String error) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(Component.text("A minecart got stuck on the railway! Location: " + loc.getX() + " " + (int) loc.getY() + " " + loc.getZ() + "\n" + error, NamedTextColor.RED)));
        cancel(); // i framtida så kommer det rød blocc og tekst som sier hva som gikk galt og at man kan ta /fix for å fikse det eller noe
        cart.remove();
    }

    public void terminate() {
        if (!cart.getPassengers().isEmpty() && cart.getPassengers().getFirst() instanceof Player p) {
            p.sendMessage(Component.text("Destination reached: " + ((Math.ceil(totalDistance / 100f))/10f) + " km", NamedTextColor.GRAY));
            CartManager.removeCart(cart.getUniqueId());
        } else if (!cart.getPassengers().isEmpty() && cart.getPassengers().getFirst() instanceof Villager villager) {
            Path path = targetLocality.getPath("station_exit"); // hardcode: alle adresses som villidgers kan komme til med rail systemet må ha en path som kalles "station_exit" som villidgersene kan gå på når de er framme.
            villager.teleport(path.getStartPos().toLocation(Main.WORLD));
            new WalkPath(villager, targetLocality, targetLocality, targetHouseNr, path).runTaskTimer(Main.getPlugin(), 0, 1);
        }
        cancel();
    }

    private void updateVelocity() {
        switch (direction) {
            case 'n' -> cart.setVelocity(new Vector(0, 0, -2 * speed)); // tallene i vektorene viser blokker per tick
            case 's' -> cart.setVelocity(new Vector(0, 0, 2 * speed));
            case 'e' -> cart.setVelocity(new Vector(2 * speed, 0, 0));
            case 'w' -> cart.setVelocity(new Vector(-2 * speed, 0, 0));
        }
    }

    private void summonMarker(Location loc, Material block) {
        Location markerloc = loc.clone();
        markerloc.add(0, -1, 0);
        ArmorStand as = (ArmorStand) Main.WORLD.spawnEntity(markerloc, EntityType.ARMOR_STAND);
        as.getEquipment().setHelmet(new ItemStack(block));
        as.setGravity(false);
        as.addScoreboardTag("test");
    }

    void syncPosition() { // telporterer til server posisjon hvis carten er for langt unna
        if (cart.isDead()) {
            cancel();
            return;
        }
        if (cart instanceof RideableMinecart) { // hvis man leaver carten midt i
            if (cart.getPassengers().isEmpty()) {
                cart.remove();
                cancel();
                return;
            }
            if (cart.getPassengers().contains(passenger)) {
                cart.removePassenger(passenger);
            }
        }
        cart.teleport(loc);
        if (cart instanceof RideableMinecart) {
            cart.addPassenger(passenger);
        }
        updateVelocity();
    }

    void trail() {
        if (counter == sectionLength) {
            summonMarker(loc, Material.RED_CONCRETE);
        } else {
            summonMarker(loc, Material.LIME_CONCRETE);
        }
    }

    void nextSection() {
        Section nextSection;
        if (currentSection.hasRoutes()) {
            RailRoute route;

            Kingdom currentKingdom = currentDistrict.getKingdom();

            District targetDistrict = targetLocality.getDistrict();
            Kingdom targetKingdom = targetDistrict.getKingdom();

            if (!Objects.equals(currentKingdom, targetKingdom)) {
                route = currentSection.getEndpointRoute(targetKingdom.id);
                if (route == null) {
                    route = currentSection.getEndpointRoute("center");
                }
            } else if (!Objects.equals(currentDistrict, targetDistrict)) {
                route = currentSection.getEndpointRoute(targetDistrict.id);
            } else {
                route = currentSection.getEndpointRoute(targetLocality.id);
            }
            if (route == null) {
                route = currentSection.getEndpointRoute("*"); // hvis det er * så betyr det alt som ikke er spesifisert i de andre rutene
                if (route == null) { // error, no route to destination
                    if (!Objects.equals(currentKingdom.id, targetKingdom.id)) {
                        terminate("Cant find route to kingdom " + targetKingdom.id);
                    } else if (!Objects.equals(currentDistrict.id, targetDistrict.id)) {
                        terminate("Cant find route to district " + targetDistrict.id);
                    } else {
                        terminate("Cant find route to address " + targetLocality.id);
                    }
                    return;
                }
            }
            nextSection = route.targetSection();

            Rail.Shape shape = route.railShape();
            Rail rail = (Rail) loc.getBlock().getBlockData();
            rail.setShape(shape);
            Main.WORLD.setBlockData(loc, rail);
        } else if (currentSection.hasNextSection()) {
            nextSection = currentSection.getNextSection();
        } else {
            // terminate, reached destination
            terminate();
            return;
        }

        if (currentSection.hasBorder()) currentDistrict = currentSection.getBorder();

        // resetter stuff og oppdaterer data til å matche den nye seksjonen
        if (sectionLength > 7 && loc.distanceSquared(cart.getLocation()) > 4) {
            if (gruveXp.getInventory().getItemInMainHand().getType() != Material.COMMAND_BLOCK) { // DEBUG, remov if statementet i fremtida
                syncPosition();
            }
        }
        counter = 0;
        currentSection = nextSection;
        nextSpeedChange = currentSection.getNextSpeedIndex(counter);
        sectionLength = currentSection.getLength();
        if (sectionLength == 0) {
            terminate("Sector " + currentSection.id + " length is not calculated!");
            return;
        }
        updateVelocity();
    }

    void move() {
        counter++;
        totalDistance++;
        Material material = loc.getBlock().getType();
        if (material == Material.AIR) {
            Material material2 = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ()).getBlock().getType();
            if (material2 == Material.RAIL || material2 == Material.POWERED_RAIL) {
                Δpos[1] = -1;
            }
        } else {
            if (material != Material.RAIL && material != Material.POWERED_RAIL) {
                terminate("Cart has derailed!");
                return;
            }
            Rail data = (Rail) loc.getBlock().getBlockData();
            for (int i = 0; i < 3; i++) { //reset
                Δpos[i] = 0;
            }
            Rail.Shape shape = data.getShape();
            switch (direction) {
                case 'n' -> {
                    switch (shape) {
                        case SOUTH_EAST -> {
                            direction = 'e';
                            speedOffset += TURN_OFFSET;
                        } // 2 måter å svinge, ellers går den bare rett fram
                        case SOUTH_WEST -> {
                            direction = 'w';
                            speedOffset += TURN_OFFSET;
                        }
                        case ASCENDING_NORTH -> Δpos[1] = 1; // hvis det er bakke så telporteres den opp/ned i tilegg
                        case ASCENDING_SOUTH -> Δpos[1] = -1;
                    }
                }
                case 's' -> {
                    switch (shape) {
                        case NORTH_EAST -> {
                            direction = 'e';
                            speedOffset += TURN_OFFSET;
                        }
                        case NORTH_WEST -> {
                            direction = 'w';
                            speedOffset += TURN_OFFSET;
                        }
                        case ASCENDING_NORTH -> Δpos[1] = -1;
                        case ASCENDING_SOUTH -> Δpos[1] = 1;
                    }
                }
                case 'e' -> {
                    switch (shape) {
                        case NORTH_WEST -> {
                            direction = 'n';
                            speedOffset += TURN_OFFSET;
                        }
                        case SOUTH_WEST -> {
                            direction = 's';
                            speedOffset += TURN_OFFSET;
                        }
                        case ASCENDING_EAST -> Δpos[1] = 1;
                        case ASCENDING_WEST -> Δpos[1] = -1;
                    }
                }
                case 'w' -> {
                    switch (shape) {
                        case NORTH_EAST -> {
                            direction = 'n';
                            speedOffset += TURN_OFFSET;
                        }
                        case SOUTH_EAST -> {
                            direction = 's';
                            speedOffset += TURN_OFFSET;
                        }
                        case ASCENDING_EAST -> Δpos[1] = -1;
                        case ASCENDING_WEST -> Δpos[1] = 1;
                    } // ellers er direction den samme
                }
            }
            switch (direction) { // endrer loc i den retninga som er "direction"
                case 'n' -> Δpos[2] = -1;
                case 's' -> Δpos[2] = 1;
                case 'e' -> Δpos[0] = 1;
                case 'w' -> Δpos[0] = -1;
            }
        }
        loc.add(Δpos[0], Δpos[1], Δpos[2]);
        if (counter % 16 == 0 && loc.distanceSquared(cart.getLocation()) > 9 && (gruveXp == null || gruveXp.getInventory().getItemInMainHand().getType() != Material.COMMAND_BLOCK)) {
            syncPosition();
        }
        // DEBUG
        if (gruveXp != null && gruveXp.getInventory().getItemInOffHand().getType() == Material.REPEATING_COMMAND_BLOCK) {
            trail();
        }
        if (counter == sectionLength) { // når minecarten har kjørt til enden av seksjonen
            nextSection();
        }
        if (counter == nextSpeedChange) {
            speed = currentSection.getSpeed(counter) / 2f;
            cart.setMaxSpeed(speed);
            Bukkit.broadcast(Component.text("Changing speed to ").append(Section.speed((int)(speed*2))));
            nextSpeedChange = currentSection.getNextSpeedIndex(counter + 1);
        }
        if (speedOffset >= 1) { // minecarten kjører litt fortere i svinger. kingdoms
            speedOffset--;
            move();
        }
    }

    @Override
    public void run() {
        moves += speed;
        while (moves >= 1) {
            move();
            moves --;
        }
    }
}
