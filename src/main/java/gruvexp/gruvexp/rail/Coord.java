package gruvexp.gruvexp.rail;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.NonNull;

public record Coord(int x, int y, int z) {

    public Coord(@JsonProperty("x") int x, @JsonProperty("y") int y, @JsonProperty("z") int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Coord(String x, String y, String z) {
        this(
                Integer.parseInt(x),
                Integer.parseInt(y),
                Integer.parseInt(z)
        );
    }

    public Coord(double x, double y, double z) {
        this(
                (int) x,
                (int) y,
                (int) z
        );
    }

    @Override
    public @NonNull String toString() {
        return String.format("%s %s %s", x, y, z);
    }

    public Component name() {
        return Component.text(toString(), NamedTextColor.AQUA)
                .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND,
                        ClickEvent.Payload.string("/java tp " + x + " " + y + " " + z)));
    }

    public Location toLocation(World world) {
        return new Location(world, x + 0.5, y, z + 0.5);
    }
}