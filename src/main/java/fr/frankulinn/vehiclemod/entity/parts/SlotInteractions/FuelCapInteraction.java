package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.item.JerricanItem;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FuelCapInteraction implements SlotInteraction {

    private static final float TRANSFER_RATE = 2.0f; // Litres par interaction

    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        if (hand == InteractionHand.OFF_HAND)
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);

        // Annuler l'interaction si ce n'est pas un jerrican
        if (!(stack.getItem() instanceof JerricanItem)) {
            return InteractionResult.PASS;
        }

        float vehicleFuel = vehicle.getEntityData().get(BaseVehicleEntity.FUEL_LEVEL);
        float jerricanFuel = JerricanItem.getFuel(stack);
        float spaceLeft = vehicle.getMaxFuel() - vehicleFuel;

        // Annuler l'interaction si le réservoir est plein
        if (spaceLeft <= 0) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§eLe réservoir est plein !"), true);
            }
            return InteractionResult.PASS;
        }

        // Annuler l'interaction si le jerrican est vide
        if (jerricanFuel <= 0) {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§cLe jerrican est vide !"), true);
            }
            return InteractionResult.PASS;
        }

        float toTransfer = Math.min(TRANSFER_RATE, Math.min(spaceLeft, jerricanFuel));

        // Augmenter le fuel du véhicule
        vehicle.getEntityData().set(BaseVehicleEntity.FUEL_LEVEL, vehicleFuel + toTransfer);

        // Diminuer le fuel du jerrican
        if (!player.isCreative()) {
            JerricanItem.setFuel(stack, jerricanFuel - toTransfer);
        }

        // Son de remplissage (1 fois sur 4 pour ne pas spammer)
        if (vehicle.tickCount % 4 == 0) {
            vehicle.level().playSound(null,
                    vehicle.getX(), vehicle.getY(), vehicle.getZ(),
                    SoundEvents.BUCKET_EMPTY, SoundSource.PLAYERS,
                    0.2f, 0.8f + vehicle.level().random.nextFloat() * 0.4f);
        }

        return InteractionResult.CONSUME;
    }
}
