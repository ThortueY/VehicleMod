package fr.frankulinn.vehiclemod.client.model;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.model.GeoModel;

/**
 * Modèle GeckoLib générique réutilisable pour les items.
 * Les ressources sont résolues automatiquement à partir du registry name :
 * <ul>
 * <li>Modèle : {@code geo/item/<id>.geo.json}</li>
 * <li>Texture : {@code textures/item/<id>.png}</li>
 * </ul>
 *
 * @param <T> Le type d'item, doit implémenter {@link GeoItem}
 */
public class ItemModel<T extends Item & GeoItem> extends GeoModel<T> {

    @Override
    public ResourceLocation getModelResource(T item) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "geo/item/" + id + ".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(T item) {
        String id = BuiltInRegistries.ITEM.getKey(item).getPath();
        return ResourceLocation.fromNamespaceAndPath("vehiclemod", "textures/item/" + id + ".png");
    }

    @Override
    public ResourceLocation getAnimationResource(T item) {
        return null;
    }
}
