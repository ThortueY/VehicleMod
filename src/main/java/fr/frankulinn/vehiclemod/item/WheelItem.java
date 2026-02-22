package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import net.minecraft.world.item.Item;

public class WheelItem extends Item {
    private final PartCategory category;

    public WheelItem(Properties properties, PartCategory category) {
        super(properties);
        this.category = category;
    }

    public PartCategory getCategory() {
        return this.category;
    }
}
