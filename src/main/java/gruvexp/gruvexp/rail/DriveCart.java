package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import gruvexp.gruvexp.core.Address;
import gruvexp.gruvexp.core.District;
import gruvexp.gruvexp.core.Kingdom;
import gruvexp.gruvexp.core.KingdomsManager;
import gruvexp.gruvexp.path.Path;
import gruvexp.gruvexp.path.WalkPath;
import org.bukkit.*;
import org.bukkit.block.data.Rail;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class DriveCart extends BukkitRunnable {

    int length;
    int totalDistance = 0;
    int counter = 0;
    char direction; // hvilken retning carten kjører. når man kommer til svinger vil den nye retninga avhengig av den forrige retninga
    final Minecart cart;
    String sectionID;
    Location loc;
    String targetKingdom;
    String kingdomID;
    String targetDistrict;
    String districtID;
    String targetAddress;
    String targetAddressNr;
    Entity passenger;
    int[] dPos = new int[3]; // ΔPos om man kjører treigt blir Δpos huska.
    float speed; // 0.5 = vanlig, 1 = motorvei, 1.5 = motorvei ekspress
    float speedOffset = 0; // svinger
    float moves = 0; // hvor mange moves som skal gjøres på veien

    // DEBUG
    Player gruvexp = Bukkit.getPlayer("GruveXp");

    public DriveCart(@NotNull Minecart cart, Entity passenger, String startKingdom, String startDistrict, String sectionID, char direction, String targetKingdom, String targetDistrict, String targetAddress) {
        this.sectionID = sectionID;
        this.cart = cart;
        this.direction = direction;
        this.passenger = passenger;
        loc = cart.getLocation();
        Kingdom kingdom = KingdomsManager.getKingdom(startKingdom);
        District district = kingdom.getDistrict(startDistrict);
        speed = district.getSection(this.sectionID).getSpeed() / 2f;
        cart.setMaxSpeed(speed);
        updateVelocity();
        kingdomID = startKingdom;
        districtID = startDistrict;
        this.targetAddress = targetAddress;
        this.targetDistrict = targetDistrict;
        this.targetKingdom = targetKingdom;
        length = district.getSection(sectionID).getLength();
        cart.addScoreboardTag("running");
    }

    public DriveCart(Villager villager, Entrypoint entrypoint, String targetKingdom, String targetDistrict, String targetAddress) { //brukes av path systemet
        this((Minecart) Main.WORLD.spawnEntity(entrypoint.getCoord().toLocation(Main.WORLD), EntityType.MINECART), villager, entrypoint.getKingdomID(), entrypoint.getDistrictID(), entrypoint.getSectionID(), entrypoint.getDirection(), targetKingdom, targetDistrict, targetAddress);
        villager.teleport(cart);
        cart.addPassenger(villager); // villageren setter seg i minecarten
        if (cart.getPassengers().get(0) instanceof Villager) {
            //Bukkit.broadcastMessage("a villager sits in the cart");
        } else {
            Bukkit.broadcastMessage(".addPassenger() is trash and dont work");
        }
        //Bukkit.broadcastMessage("as you clearly see, the villager is now sitting in the minecart");
    }

    public void terminate(String error) {
        Bukkit.getOnlinePlayers().forEach(p -> p.sendMessage(ChatColor.RED + "A minecart got stuck on the railway! Location: " + loc.getX() + " " + (int) loc.getY() + " " + loc.getZ() + "\n" + error));
        cancel(); // i framtida så kommer det rød blocc og tekst som sier hva som gikk galt og at man kan ta /fix for å fikse det eller noe
        cart.remove();
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
        if (cart.isDead() || cart.getPassengers().isEmpty()) { // hvis man leaver carten midt i
            cancel();
            cart.remove();
            return;
        }
        if (cart.getPassengers().contains(passenger)) {
            cart.removePassenger(passenger);
        }
        cart.teleport(loc);
        cart.addPassenger(passenger);
        updateVelocity();
    }

    void trail() {
        if (counter == length) {
            summonMarker(loc, Material.RED_CONCRETE);
        } else {
            summonMarker(loc, Material.LIME_CONCRETE);
        }
    }

    void nextSection() {
        Kingdom kingdom = KingdomsManager.getKingdom(kingdomID);
        District district = kingdom.getDistrict(districtID);
        String[] endpointdata; // [dir, next_section]
        Section section = district.getSection(sectionID);
        if (!Objects.equals(kingdomID, targetKingdom)) {
            endpointdata = section.getEndpointData(targetKingdom);
            if (endpointdata == null) {
                endpointdata = section.getEndpointData("center");
            }
        } else if (!Objects.equals(districtID, targetDistrict)) {
            endpointdata = section.getEndpointData(targetDistrict);
        } else {
            endpointdata = section.getEndpointData(targetAddress);
        }
        if (endpointdata == null) {
            endpointdata = section.getEndpointData("*"); // hvis det er * så betyr det alt som ikke er spesifisert i de andre rutene
            if (endpointdata == null) {
                if (!Objects.equals(kingdomID, targetKingdom)) {
                    terminate(ChatColor.RED + "Cant find route to kingdom " + ChatColor.BOLD + kingdomID);
                } else if (!Objects.equals(districtID, targetDistrict)) {
                    terminate(ChatColor.RED + "Cant find route to district " + ChatColor.BOLD + districtID);
                } else {
                    terminate(ChatColor.RED + "Cant find route to address " + ChatColor.BOLD + targetAddress);
                }
                return;
            }
        }
        String nextSectionID = endpointdata[1];
        if (Objects.equals(nextSectionID, "end") || nextSectionID.contains("end:")) { // hvis monoroute blir satt til end/stop så slutter railwayen.
            if (!cart.getPassengers().isEmpty() && cart.getPassengers().get(0) instanceof Player) {
                Player p = (Player) cart.getPassengers().get(0);
                p.sendMessage(ChatColor.GRAY + "Destination reached: " + ((Math.ceil(totalDistance / 100f))/10f) + " km");
                CartManager.removeCart(cart.getUniqueId());
            } else if (!cart.getPassengers().isEmpty() && cart.getPassengers().get(0) instanceof Villager) {
                Address address = district.getAddress(targetAddress);
                Path path = address.getPath("station_exit"); // hardcode: alle adresses som villidgers kan komme til med rail systemet må ha en path som kalles "station_exit" som villidgersene kan gå på når de er framme.
                Villager villager = (Villager) cart.getPassengers().get(0);
                villager.teleport(path.getStartPos().toLocation(Main.WORLD));
                new WalkPath(villager, kingdomID, districtID, targetAddress, kingdomID, districtID, targetAddress, targetAddressNr, path).runTaskTimer(Main.getPlugin(), 0, 1);
            }
            cancel();
            return;
        }
        String dir = endpointdata[0];
        if (dir != null) {
            //Rail.Shape shape = KingdomsManager.string2Rail.get(section.getShape(dir));
            Rail.Shape shape = Rail.Shape.valueOf(section.getShape(dir));
            Rail rail = (Rail) loc.getBlock().getBlockData();
            assert shape != null;
            rail.setShape(shape);
            Main.WORLD.setBlockData(loc, rail);
        }

        String borderKingdomID = section.getBorderKingdom();
        String borderDistrictID = section.getBorderDistrict();
        if (borderDistrictID != null) { // oppdaterer borders hvis det er noen endring
            if (borderKingdomID != null) {
                kingdomID = borderKingdomID;
                kingdom = KingdomsManager.getKingdom(kingdomID);
            }
            districtID = borderDistrictID;
            district = kingdom.getDistrict(districtID);
        }

        if (district.notContainsSection(nextSectionID)) {
            terminate(ChatColor.RED + "Section " + ChatColor.GOLD + kingdomID + " : " + districtID + ChatColor.RED + " : " + nextSectionID + " doesnt exist");
            return;
        }
        // resetter stuff og oppdaterer data til å matche den nye seksjonen
        if (length > 7 && loc.distance(cart.getLocation()) > 2) {
            if (gruvexp.getInventory().getItemInMainHand().getType() != Material.COMMAND_BLOCK) { // DEBUG, remov if statementet i fremtida
                syncPosition();
            }
        }
        counter = 0;
        sectionID = nextSectionID;
        section = district.getSection(sectionID);
        length = section.getLength();
        if (length == 0) {
            terminate(ChatColor.RED + "Sector " + sectionID + " length is not calculated!");
            return;
        }
        speed = section.getSpeed() / 2f;
        cart.setMaxSpeed(speed);
        updateVelocity();
    }

    void move() {
        counter++;
        totalDistance++;
        Material material = loc.getBlock().getType();
        if (material == Material.AIR) {
            Material material2 = new Location(loc.getWorld(), loc.getX(), loc.getY() - 1, loc.getZ()).getBlock().getType();
            if (material2 == Material.RAIL || material2 == Material.POWERED_RAIL) {
                dPos[1] = -1;
            }
        } else {
            if (material != Material.RAIL && material != Material.POWERED_RAIL) {
                terminate("Cart has derailed!");
                return;
            }
            Rail data = (Rail) loc.getBlock().getBlockData();
            for (int i = 0; i < 3; i++) { //reset
                dPos[i] = 0;
            }
            Rail.Shape shape = data.getShape();
            switch (direction) {
                case 'n' -> {
                    switch (shape) {
                        case SOUTH_EAST -> {
                            direction = 'e';
                            speedOffset += 0.5F;
                        } // 2 måter å svinge, ellers går den bare rett fram
                        case SOUTH_WEST -> {
                            direction = 'w';
                            speedOffset += 0.5F;
                        }
                        case ASCENDING_NORTH -> dPos[1] = 1; // hvis det er bakke så telporteres den opp/ned i tilegg
                        case ASCENDING_SOUTH -> dPos[1] = -1;
                    }
                }
                case 's' -> {
                    switch (shape) {
                        case NORTH_EAST -> {
                            direction = 'e';
                            speedOffset += 0.5F;
                        }
                        case NORTH_WEST -> {
                            direction = 'w';
                            speedOffset += 0.5F;
                        }
                        case ASCENDING_NORTH -> dPos[1] = -1;
                        case ASCENDING_SOUTH -> dPos[1] = 1;
                    }
                }
                case 'e' -> {
                    switch (shape) {
                        case NORTH_WEST -> {
                            direction = 'n';
                            speedOffset += 0.5F;
                        }
                        case SOUTH_WEST -> {
                            direction = 's';
                            speedOffset += 0.5F;
                        }
                        case ASCENDING_EAST -> dPos[1] = 1;
                        case ASCENDING_WEST -> dPos[1] = -1;
                    }
                }
                case 'w' -> {
                    switch (shape) {
                        case NORTH_EAST -> {
                            direction = 'n';
                            speedOffset += 0.5F;
                        }
                        case SOUTH_EAST -> {
                            direction = 's';
                            speedOffset += 0.5F;
                        }
                        case ASCENDING_EAST -> dPos[1] = -1;
                        case ASCENDING_WEST -> dPos[1] = 1;
                    } // ellers er direction den samme
                }
            }
            switch (direction) { // endrer loc i den retninga som er "direction"
                case 'n' -> dPos[2] = -1;
                case 's' -> dPos[2] = 1;
                case 'e' -> dPos[0] = 1;
                case 'w' -> dPos[0] = -1;
            }
        }
        loc.add(dPos[0], dPos[1], dPos[2]);
        if (counter % 16 == 0 && loc.distance(cart.getLocation()) > 3 && (gruvexp == null || gruvexp.getInventory().getItemInMainHand().getType() != Material.COMMAND_BLOCK)) {
            syncPosition();
        }
        // DEBUG
        if (gruvexp != null && gruvexp.getInventory().getItemInOffHand().getType() == Material.REPEATING_COMMAND_BLOCK) {
            trail();
        }
        if (counter == length) { // når minecarten har kjørt til enden av seksjonen
            nextSection();
        }
        if (speedOffset >= 1) { // minecarten kjører litt fortere i svinger. kiŋdoms
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
