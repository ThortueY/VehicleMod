package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WheelModel extends GeoModel<VehicleEntity> {

    // Cette variable va changer selon la roue qu'on est en train de dessiner !
    private String currentWheelType = "offroad_wheel";

    public void setWheelType(String type) {
        this.currentWheelType = type;
    }

    @Override
    public ResourceLocation getModelResource(VehicleEntity object) {
        // Ex: "geo/wheel_offroad.geo.json"
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/" + currentWheelType + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(VehicleEntity object) {
        // Ex: "textures/entity/wheel_offroad.png"
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/" + currentWheelType + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(VehicleEntity animatable) {
        return null; // Pas d'animation pour l'instant
    }
}
