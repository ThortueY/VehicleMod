package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EngineModel extends GeoModel<VehicleEntity> {
    @Override
    public ResourceLocation getModelResource(VehicleEntity object) {
        // Remplace par le nom de ton fichier json
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/engine.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VehicleEntity object) {
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/engine.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VehicleEntity animatable) {
        return null; // Pas d'animation pour l'instant
    }
}
