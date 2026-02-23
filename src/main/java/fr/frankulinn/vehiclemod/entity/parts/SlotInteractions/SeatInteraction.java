package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SeatPart;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.item.SeatItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class SeatInteraction implements SlotInteraction {
    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        // 1. SI L'EMPLACEMENT EST VIDE
        if (slot.isEmpty()) {
            if (stackInHand.getItem() instanceof SeatItem seatItem) {
                // Vérification de la catégorie (ex: On ne met pas une selle de vélo dans une voiture)
                if (seatItem.getCategory() != slot.getAllowedCategory()) {
                    if (!vehicle.level().isClientSide()) player.displayClientMessage(Component.literal("§cCe siège n'est pas adapté à ce véhicule !"), true);
                    return InteractionResult.SUCCESS;
                }

                SeatPart newSeat = seatItem.createPart();
                if (slot.installPart(newSeat)) {
                    if (!player.isCreative()) stackInHand.shrink(1);
                    if (!vehicle.level().isClientSide()) player.displayClientMessage(Component.literal("§aSiège posé ! (Utilisez une clé pour le fixer)"), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            }
        }
        // 2. SI LE SIÈGE EST POSÉ MAIS PAS FIXÉ
        else if (!slot.isSecured()) {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.securePart()) {
                    if (!vehicle.level().isClientSide()) player.displayClientMessage(Component.literal("§aSiège fixé !"), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            } else if (stackInHand.isEmpty() && player.isShiftKeyDown()) { // Shift + Clic main vide pour retirer
                slot.removePart();
                if (!vehicle.level().isClientSide()) player.displayClientMessage(Component.literal("§eSiège retiré !"), true);
                vehicle.updatePartsSync();
                return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
            }
        }
        // 3. SI LE SIÈGE EST FIXÉ
        else {
            if (stackInHand.getItem() instanceof WrenchItem) {
                // ... (Ton code actuel pour dévisser le siège avec la clé) ...
                if (slot.unsecurePart()) {
                    if (!vehicle.level().isClientSide()) player.displayClientMessage(Component.literal("§eSiège dévissé."), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            }
            // --- NOUVEAU : S'ASSEOIR SPÉCIFIQUEMENT SUR CE SIÈGE ---
            else if (stackInHand.isEmpty() && !player.isShiftKeyDown()) {

                // On vérifie que personne n'est déjà assis à cette place
                if (!vehicle.isSeatOccupied(slot.getId())) {
                    if (!vehicle.level().isClientSide()) {
                        // On lui réserve la place, puis on le fait monter !
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
        return InteractionResult.PASS; // On laisse passer l'interaction si ce n'est pas lié aux pièces (pour pouvoir monter !)
    }
}
