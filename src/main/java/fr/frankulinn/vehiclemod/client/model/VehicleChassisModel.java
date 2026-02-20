package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class VehicleChassisModel extends GeoModel<VehicleEntity> {

    @Override
    public ResourceLocation getModelResource(VehicleEntity object) {
        // Chemin vers ton fichier exporté de Blockbench : src/main/resources/assets/Vehiclemod/geo/vehicle_chassis.geo.json
        return ResourceLocation.fromNamespaceAndPath(Vehiclemod.MODID, "geo/vehicle_chassis.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VehicleEntity object) {
        // Chemin vers ta texture : src/main/resources/assets/Vehiclemod/textures/entity/vehicle_chassis.png
        return ResourceLocation.fromNamespaceAndPath(Vehiclemod.MODID, "textures/entity/vehicle_chassis.png");
    }

    @Override
    public ResourceLocation getAnimationResource(VehicleEntity animatable) {
        // Chemin vers les animations : src/main/resources/assets/Vehiclemod/animations/vehicle_chassis.animation.json
        return ResourceLocation.fromNamespaceAndPath(Vehiclemod.MODID, "animations/vehicle_chassis.animation.json");
    }
}
