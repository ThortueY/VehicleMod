package fr.frankulinn.vehiclemod.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;

public class JerricanItem extends Item {
    public static final float MAX_FUEL = 100.0f;

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
        CustomData.update(DataComponents.CUSTOM_DATA, stack,
                tag -> tag.putFloat("Fuel", Math.max(0, net.minecraft.util.Mth.clamp(fuel, 0, MAX_FUEL))));
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
}
