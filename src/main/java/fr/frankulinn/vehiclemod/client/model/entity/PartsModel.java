package fr.frankulinn.vehiclemod.client.model.entity;

import fr.frankulinn.vehiclemod.client.model.ModelSubPath;
import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class PartsModel extends GeoModel<BaseVehicleEntity> {

    private String partId;
    private String subPath;

    public void init( ModelSubPath subPath) {
        this.partId = "none";
        this.subPath = subPath.getSubPath();
    }

    public void setPartId(String partId) {
        this.partId = partId;
    }

    @Override
    public ResourceLocation getModelResource(BaseVehicleEntity object) {
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/" + subPath + "/"+ partId + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(BaseVehicleEntity object) {
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/" + subPath + "/" + partId +".png");
    }

    @Override
    public ResourceLocation getAnimationResource(BaseVehicleEntity animatable) {
        return null;
    }
}
