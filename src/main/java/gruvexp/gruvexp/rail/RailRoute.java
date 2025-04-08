package gruvexp.gruvexp.rail;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.data.Rail;

public record RailRoute(Section targetSection, Rail.Shape railShape, String direction) {

    public Component name() {
        return Component.empty().append(targetSection.name())
                .append(Component.text(", direction ")).append(Component.text(direction))
                .append(Component.text(", rail changes to ")).append(Component.text(railShape.toString(), NamedTextColor.BLUE));
    }

}
