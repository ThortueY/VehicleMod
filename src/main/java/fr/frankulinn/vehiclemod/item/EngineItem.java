package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.entity.parts.EnginePart;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class EngineItem extends Item {
    private final float hp;
    private final float weight;
    private final float maxSpeed;
    private final float fuelConso;
    private final PartCategory partCategory;

    public EngineItem(Properties properties, PartCategory partCategory, float hp, float weight, float maxSpeed, float fuelConso) {
        super(properties);
        this.hp = hp;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.fuelConso = fuelConso;
        this.partCategory = partCategory;
    }

    public EnginePart createPart() {
        String modelId = BuiltInRegistries.ITEM.getKey(this).getPath();
        return new EnginePart(this.hp, this.weight, this.maxSpeed, this.fuelConso);
    }


    public PartCategory getPartCategory() {
        return partCategory;
    }
}
