package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class EngineModel extends GeoModel<BaseVehicleEntity> {
    private String engineId;

    public void setEngineId(String engineId) {
        this.engineId = engineId;
    }

    @Override
    public ResourceLocation getModelResource(BaseVehicleEntity object) {
        // Remplace par le nom de ton fichier json
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/"+ engineId + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseVehicleEntity object) {
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/"+ engineId +".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseVehicleEntity animatable) {
        return null; // Pas d'animation pour l'instant
    }
}
