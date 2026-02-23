package fr.frankulinn.vehiclemod.client.model;


import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SeatModel extends GeoModel<BaseVehicleEntity> {

    // Cette variable va changer selon la roue qu'on est en train de dessiner !
    private String currentSeatType = "basic_seat";

    public void setSeatId(String type) {
        this.currentSeatType = type;
    }

    @Override
    public ResourceLocation getModelResource(BaseVehicleEntity object) {
        // Ex: "geo/wheel_offroad.geo.json"
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/entity/vehicle_seats/" + currentSeatType + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseVehicleEntity object) {
        // Ex: "textures/entity/wheel_offroad.png"
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/vehicle_seats/" + currentSeatType + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseVehicleEntity animatable) {
        return null; // Pas d'animation pour l'instant
    }
}

