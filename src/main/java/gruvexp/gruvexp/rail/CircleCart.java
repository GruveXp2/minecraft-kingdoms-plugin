package gruvexp.gruvexp.rail;

import gruvexp.gruvexp.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class CircleCart {

    private final Minecart cart;
    private final double radius;
    private final double speed;
    private static final DecimalFormat df = new DecimalFormat("0.00");
    Player gruveXp = Bukkit.getPlayer("GruveXp");

    public CircleCart(Minecart cart, double radius, double speed) {
        this.cart = cart;
        this.radius = radius;
        this.speed = speed / 20; // En runde tar x sekunder
        startCircle();
    }

    private void startCircle() {
        cart.setMaxSpeed(speed);
        new BukkitRunnable() {
            double t = 0;
            final Vector direction = new Vector();

            public void run() {
                // Beregn ny posisjon på minecarten
                direction.setX(Math.cos(t));
                direction.setZ(Math.sin(t));
                direction.setY(0);

                // Sett fart og retning på minecarten
                direction.normalize().multiply(speed); // normalisert = sett lengden til 1 men behold vinkel, så gang inn speeded
                cart.setVelocity(direction);
                if (gruveXp != null && gruveXp.getInventory().getItemInMainHand().getType() == Material.COMMAND_BLOCK) {
                    gruveXp.sendMessage(String.format("Vel:[%s, %s], t = %s, Δt = %s", df.format(direction.getX()), df.format(direction.getZ()), df.format(t), df.format(Math.PI * speed / (10 * radius))));
                }
                if (gruveXp != null && gruveXp.getInventory().getItemInMainHand().getType() == Material.REPEATING_COMMAND_BLOCK) {
                    cancel();
                }

                t += Math.PI * speed / (10 * radius); // Tilsvarer 1/20 av en runde (1 rps
                // )

                if (t >= 2.0 * Math.PI) { // 2π rad = 360°
                    t -= 2.0 * Math.PI;
                }
            }
        }.runTaskTimer(Main.getPlugin(), 0, 1);
    }
}