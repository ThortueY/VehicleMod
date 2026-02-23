package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.item.EngineItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EngineItemModel extends GeoModel<EngineItem> {

    // L'ID sera résolu dynamiquement à partir du registre de l'item
    // Ex: "kart_engine" → geo/entity/vehicle_engines/kart_engine.geo.json

    @Override
    public ResourceLocation getModelResource(EngineItem item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/entity/vehicle_engines/" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(EngineItem item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/vehicle_engines/" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(EngineItem item) {
        return null; // Pas d'animation pour l'instant
    }
}
