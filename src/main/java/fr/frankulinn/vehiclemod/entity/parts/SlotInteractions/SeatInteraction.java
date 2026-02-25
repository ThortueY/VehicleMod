package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SeatPart;
import fr.frankulinn.vehiclemod.item.SeatItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SeatInteraction extends AbstractSlotInteraction<SeatItem, SeatPart> {

    @Override
    protected boolean isValidItem(ItemStack stack) {
        return stack.getItem() instanceof SeatItem;
    }

    @Override
    protected boolean isCategoryValid(SeatItem item, PartSlot slot) {
        return item.getCategory() == slot.getAllowedCategory();
    }

    @Override
    protected SeatPart createPart(SeatItem item) {
        return item.createPart();
    }

    @Override
    protected String getPartName() {
        return "Siège";
    }

    @Override
    protected String getInvalidCategoryMessage() {
        return "§cCe siège n'est pas adapté à ce véhicule !";
    }

    @Override
    protected InteractionResult onSecuredInteract(Player player, InteractionHand hand, PartSlot slot,
            BaseVehicleEntity vehicle) {
        if (vehicle.isSeatNotOccupied(slot.getId())) {
            if (!vehicle.level().isClientSide()) {
                vehicle.assignSeat(player, slot.getId());
                player.startRiding(vehicle);
            }
            return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
        } else {
            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§cCe siège est déjà occupé !"), true);
            }
            return InteractionResult.SUCCESS;
        }
    }
}
