package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import fr.frankulinn.vehiclemod.entity.parts.WheelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;

public class WheelItem extends Item {
    private final float baseGrip;
    private final float baseWeight;
    private final PartCategory partCategory;


    // Le constructeur prend les stats de la roue
    public WheelItem(Properties properties, PartCategory partCategory, float baseGrip, float baseWeight) {
        super(properties);
        this.baseGrip = baseGrip;
        this.baseWeight = baseWeight;
        this.partCategory = partCategory;
    }

    // L'usine qui crée la pièce pour le slot !
    public WheelPart createPart() {
        // Magie : on récupère l'ID exact tel que tu l'as déclaré dans ModItems ! (ex: "kart_wheel")
        String modelId = BuiltInRegistries.ITEM.getKey(this).getPath();
        return new WheelPart(this.baseGrip, this.baseWeight, modelId);
    }

    public PartCategory getPartCategory() {
        return partCategory;
    }
}
