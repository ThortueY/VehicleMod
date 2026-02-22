package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.item.JerricanItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class FuelCapInteraction implements SlotInteraction {
    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, VehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        if (stackInHand.getItem() instanceof fr.frankulinn.vehiclemod.item.JerricanItem) {
            float currentFuel = vehicle.getEntityData().get(VehicleEntity.FUEL_LEVEL);

            if (currentFuel < VehicleEntity.MAX_FUEL) {
                vehicle.getEntityData().set(VehicleEntity.FUEL_LEVEL, VehicleEntity.MAX_FUEL);
                if (!player.isCreative()) stackInHand.shrink(1);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aPlein effectué ! ⛽ (100%)"), true);
            } else {
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eLe réservoir est déjà plein !"), true);
            }
        } else {
            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cC'est la trappe à essence. Utilisez un Jerrican !"), true);
        }

        return InteractionResult.SUCCESS;
    }

}
