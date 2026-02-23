package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.EnginePart;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.item.EngineItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EngineBayInteraction implements SlotInteraction {
    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        // 1. SI L'EMPLACEMENT EST VIDE
        if (slot.isEmpty()) {
            if (stackInHand.getItem() instanceof EngineItem engineItem) {
                if (engineItem.getPartCategory() != slot.getAllowedCategory()) {
                    if (!vehicle.level().isClientSide())
                        player.displayClientMessage(Component.literal("§cCe moteur n'est pas adapté à ce véhicule !"),
                                true);
                    return InteractionResult.SUCCESS;
                }

                EnginePart newEngine = engineItem.createPart();
                if (slot.installPart(newEngine)) {
                    if (!player.isCreative())
                        stackInHand.shrink(1);
                    if (!vehicle.level().isClientSide()) {
                        player.displayClientMessage(Component.literal("§aMoteur posé ! (Non fixé)"), true);
                        vehicle.updatePartsSync();
                    }
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            } else if (!stackInHand.isEmpty()) {
                if (!vehicle.level().isClientSide())
                    player.displayClientMessage(Component.literal("§cCet emplacement nécessite un Moteur !"), true);
                return InteractionResult.SUCCESS;
            }
        }
        // 2. SI LE MOTEUR EST POSÉ MAIS PAS FIXÉ
        else if (!slot.isSecured()) {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.securePart()) {
                    if (!vehicle.level().isClientSide()) {
                        player.displayClientMessage(Component.literal("§aMoteur fixé et prêt à l'usage !"), true);
                        vehicle.updatePartsSync();
                    }
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            }
            // --- LE FIX EST ICI ---
            else if (stackInHand.isEmpty() && player.isShiftKeyDown()) {
                slot.removePart();
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal("§eMoteur retiré !"), true);
                    vehicle.updatePartsSync();
                }
                return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
            } else {
                if (!vehicle.level().isClientSide())
                    player.displayClientMessage(
                            Component.literal("§cUtilisez une Clé pour fixer, ou Shift + Mains vides pour enlever."),
                            true);
                return InteractionResult.SUCCESS;
            }
        }
        // 3. SI LE MOTEUR EST FIXÉ
        else {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.unsecurePart()) {
                    if (!vehicle.level().isClientSide()) {
                        player.displayClientMessage(Component.literal("§eMoteur dévissé."), true);
                        vehicle.updatePartsSync();
                    }
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            } else {
                if (!vehicle.level().isClientSide())
                    player.displayClientMessage(Component.literal("§cCette pièce est vissée ! Utilisez une Clé."),
                            true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }
}