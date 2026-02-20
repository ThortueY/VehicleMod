package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.GeoBone;
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

    @Override
    public void setCustomAnimations(VehicleEntity animatable, long instanceId, AnimationState<VehicleEntity> animationState) {
        super.setCustomAnimations(animatable, instanceId, animationState);

        // 1. Le Moteur (Classique)
        GeoBone engineBone = this.getAnimationProcessor().getBone("engine");
        if (engineBone != null) {
            engineBone.setHidden(!animatable.getEntityData().get(VehicleEntity.HAS_ENGINE));
        }

        // 2. Les Roues (Dynamique)
        updateWheelVisibility("wheel_fl", animatable.getEntityData().get(VehicleEntity.WHEEL_FL));
        updateWheelVisibility("wheel_fr", animatable.getEntityData().get(VehicleEntity.WHEEL_FR));
        updateWheelVisibility("wheel_bl", animatable.getEntityData().get(VehicleEntity.WHEEL_BL));
        updateWheelVisibility("wheel_br", animatable.getEntityData().get(VehicleEntity.WHEEL_BR));
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
