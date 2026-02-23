package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.client.renderer.EngineItemRenderer;
import fr.frankulinn.vehiclemod.entity.parts.EnginePart;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;

import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

public class EngineItem extends Item implements GeoItem {
    private final float hp;
    private final float weight;
    private final float maxSpeed;
    private final float fuelConso;
    private final PartCategory partCategory;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public EngineItem(Properties properties, PartCategory partCategory, float hp, float weight, float maxSpeed,
            float fuelConso) {
        super(properties);
        this.hp = hp;
        this.weight = weight;
        this.maxSpeed = maxSpeed;
        this.fuelConso = fuelConso;
        this.partCategory = partCategory;
    }

    public EnginePart createPart() {
        String id = BuiltInRegistries.ITEM.getKey(this).getPath();
        return new EnginePart(this.hp, this.weight, this.maxSpeed, this.fuelConso, id);
    }

    public PartCategory getPartCategory() {
        return partCategory;
    }

    // --- GeoItem ---

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private EngineItemRenderer renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new EngineItemRenderer();
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
