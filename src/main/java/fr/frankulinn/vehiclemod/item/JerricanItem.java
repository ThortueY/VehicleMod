package fr.frankulinn.vehiclemod.item;

import fr.frankulinn.vehiclemod.client.model.item.ItemModel;
import fr.frankulinn.vehiclemod.client.renderer.ItemRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Consumer;

public class JerricanItem extends Item implements GeoItem {
    public static final float MAX_FUEL = 100.0f;

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public JerricanItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static float getFuel(ItemStack stack) {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        if (!data.contains("Fuel"))
            return MAX_FUEL;
        return data.copyTag().getFloat("Fuel");
    }

    public static void setFuel(ItemStack stack, float fuel) {
        float clamped = net.minecraft.util.Mth.clamp(fuel, 0, MAX_FUEL);
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.putFloat("Fuel", clamped);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        float fuel = getFuel(stack);
        return Math.round(13.0f * (fuel / MAX_FUEL));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xFFAA00;
    }

    @Override
    public int getUseDuration(ItemStack stack, net.minecraft.world.entity.LivingEntity entity) {
        return 72000; // Permet de maintenir le clic droit (1 heure in-game = infini)
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents,
            TooltipFlag tooltipFlag) {
        float fuel = getFuel(stack);
        String fuelText = String.format("%.1f / %.1f L", fuel, MAX_FUEL);

        ChatFormatting color;
        if (fuel <= 0) {
            color = ChatFormatting.RED;
        } else if (fuel < MAX_FUEL * 0.25f) {
            color = ChatFormatting.GOLD;
        } else {
            color = ChatFormatting.GREEN;
        }

        tooltipComponents.add(Component.literal("⛽ " + fuelText).withStyle(color));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    // --- GeoItem ---

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private ItemRenderer<JerricanItem> renderer;

            @Override
            public net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new ItemRenderer<>(new ItemModel<>());
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
