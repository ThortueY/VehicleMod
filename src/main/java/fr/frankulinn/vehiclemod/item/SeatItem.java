package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.client.model.ModelSubPath;
import fr.frankulinn.vehiclemod.client.model.item.PartItemModel;
import fr.frankulinn.vehiclemod.client.renderer.ItemRenderer;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import fr.frankulinn.vehiclemod.entity.parts.SeatPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class SeatItem extends Item implements GeoItem {
    private final float baseWeight;
    private final PartCategory category;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public SeatItem(Properties properties, float baseWeight, PartCategory category) {
        super(properties);
        this.baseWeight = baseWeight;
        this.category = category;
    }

    public PartCategory getCategory() {
        return this.category;
    }

    public SeatPart createPart() {
        String modelId = BuiltInRegistries.ITEM.getKey(this).getPath();
        return new SeatPart(this.baseWeight, modelId);
    }

    // --- GeoItem ---

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ItemRenderer<EngineItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null) {
                    // On utilise le modèle d'Item universel en lui donnant le chemin de tes fichiers moteurs !
                    this.renderer = new ItemRenderer<>(new PartItemModel<>(ModelSubPath.ENTITY_SEATS));
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // Pas d'animation pour l'instant
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
