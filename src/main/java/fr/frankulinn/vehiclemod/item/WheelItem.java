package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.client.renderer.WheelItemRenderer;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import fr.frankulinn.vehiclemod.entity.parts.WheelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class WheelItem extends Item implements GeoItem {
    private final float baseGrip;
    private final float baseWeight;
    private final PartCategory partCategory;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public WheelItem(Properties properties, PartCategory partCategory, float baseGrip, float baseWeight) {
        super(properties);
        this.baseGrip = baseGrip;
        this.baseWeight = baseWeight;
        this.partCategory = partCategory;
    }

    public WheelPart createPart() {
        String id = BuiltInRegistries.ITEM.getKey(this).getPath();
        return new WheelPart(this.baseGrip, this.baseWeight, id);
    }

    public PartCategory getPartCategory() {
        return partCategory;
    }

    // --- GeoItem ---

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private WheelItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new WheelItemRenderer();
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
