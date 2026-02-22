package fr.frankulinn.vehiclemod.entity.parts;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface SlotInteraction {
    InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, VehicleEntity vehicle);
}
