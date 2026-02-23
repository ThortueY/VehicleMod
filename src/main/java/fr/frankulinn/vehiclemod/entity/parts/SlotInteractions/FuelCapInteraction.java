package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.item.JerricanItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FuelCapInteraction implements SlotInteraction {

    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        if (hand == InteractionHand.OFF_HAND)
            return InteractionResult.PASS;

        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof JerricanItem) {

            float vehicleFuel = vehicle.getEntityData().get(BaseVehicleEntity.FUEL_LEVEL);
            float jerricanFuel = JerricanItem.getFuel(stack);

            float maxVehicleFuel = vehicle.getMaxFuel();
            float spaceLeft = maxVehicleFuel - vehicleFuel;

            if (spaceLeft <= 0) {
                if (!player.level().isClientSide()) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§eLe réservoir est plein !"), true);
                }
                return InteractionResult.PASS;
            } else if (jerricanFuel <= 0) {
                if (!player.level().isClientSide()) {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§cLe jerrican est vide !"), true);
                }
                return InteractionResult.PASS;
            }

            // On démarre le remplissage continu ! (Géré dans le tick() du véhicule)
            vehicle.startRefueling(player, hand);

            return InteractionResult.CONSUME;

        } else {
            if (!player.level().isClientSide()) {
                player.displayClientMessage(net.minecraft.network.chat.Component
                        .literal("§cC'est la trappe à essence. Utilisez un Jerrican !"), true);
            }
        }

        return InteractionResult.PASS;
    }

}
