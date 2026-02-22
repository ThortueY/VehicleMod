package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WheelModel extends GeoModel<BaseVehicleEntity> {

    // Cette variable va changer selon la roue qu'on est en train de dessiner !
    private String currentWheelType = "offroad_wheel";

    public void setWheelType(String type) {
        this.currentWheelType = type;
    }

    @Override
    public ResourceLocation getModelResource(BaseVehicleEntity object) {
        // Ex: "geo/wheel_offroad.geo.json"
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/entity/vehicle_wheels/" + currentWheelType + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseVehicleEntity object) {
        // Ex: "textures/entity/wheel_offroad.png"
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/vehicle_wheels/" + currentWheelType + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseVehicleEntity animatable) {
        return null; // Pas d'animation pour l'instant
    }
}
