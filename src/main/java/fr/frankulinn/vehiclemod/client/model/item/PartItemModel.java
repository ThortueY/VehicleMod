package fr.frankulinn.vehiclemod.client.model.item;

import fr.frankulinn.vehiclemod.client.model.ModelSubPath;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

public class PartItemModel<T extends Item & GeoItem> extends GeoModel<T> {
    private final String subPath;

    // On demande le nom du dossier lors de la création (ex: "entity/vehicle_engines")
    public PartItemModel(ModelSubPath subPath) {
        this.subPath = subPath.getSubPath();
    }

    @Override
    public ResourceLocation getModelResource(T item) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/" + subPath + "/" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T item) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/" + subPath + "/" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return null;
    }
}
