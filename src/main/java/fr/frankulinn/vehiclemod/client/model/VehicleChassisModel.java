package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;

public class VehicleChassisModel extends GeoModel<BaseVehicleEntity> {

    @Override
    public ResourceLocation getModelResource(BaseVehicleEntity object) {
        // On récupère l'ID exact de l'entité (ex: "kart", "bicycle", "truck")
        String vehicleId = BuiltInRegistries.ENTITY_TYPE.getKey(object.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(Vehiclemod.MODID, "geo/entity/vehicle_chassis/" + vehicleId + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseVehicleEntity object) {
        String vehicleId = BuiltInRegistries.ENTITY_TYPE.getKey(object.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(Vehiclemod.MODID, "textures/entity/vehicle_chassis/" + vehicleId + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseVehicleEntity object) {
        String vehicleId = BuiltInRegistries.ENTITY_TYPE.getKey(object.getType()).getPath();
        return ResourceLocation.fromNamespaceAndPath(Vehiclemod.MODID, "animations/entity/vehicle_chassis/" + vehicleId + ".animation.json");
    }

    @Override
    public void setCustomAnimations(BaseVehicleEntity animatable, long instanceId, AnimationState<BaseVehicleEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // On gère uniquement le volant
        GeoBone steeringWheel = this.getAnimationProcessor().getBone("steering_wheel");
        if (steeringWheel != null) {
            float steeringRadians = animatable.steeringAngle * ((float) Math.PI / 180F);
            steeringWheel.setRotZ(steeringRadians);
        }
    }


    // Méthode utilitaire pour gérer toutes les roues facilement
    private void updateWheelVisibility(String bonePrefix, String activeWheelType) {
        // Liste de toutes tes roues modélisées dans Blockbench
        String[] allWheelTypes = {"offroad", "kart", "bike"};

        for (String type : allWheelTypes) {
            GeoBone wheelVariantBone = this.getAnimationProcessor().getBone(bonePrefix + "_" + type);
            if (wheelVariantBone != null) {
                // Si le type correspond à la roue installée, on l'affiche (hidden = false), sinon on cache (hidden = true)
                wheelVariantBone.setHidden(!type.equals(activeWheelType));
            }
        }
    }
}
