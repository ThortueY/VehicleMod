package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.item.WheelItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WheelItemModel extends GeoModel<WheelItem> {

    @Override
    public ResourceLocation getModelResource(WheelItem item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/entity/vehicle_wheels/" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WheelItem item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/vehicle_wheels/" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(WheelItem item) {
        return null;
    }
}
