package fr.frankulinn.vehiclemod.client.model;

import fr.frankulinn.vehiclemod.item.SeatItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SeatItemModel extends GeoModel<SeatItem> {

    @Override
    public ResourceLocation getModelResource(SeatItem item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/entity/vehicle_seats/" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(SeatItem item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/entity/vehicle_seats/" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(SeatItem item) {
        return null;
    }
}
