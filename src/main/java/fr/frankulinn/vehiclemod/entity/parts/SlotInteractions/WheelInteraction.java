package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.entity.parts.WheelPart;
import fr.frankulinn.vehiclemod.item.WheelItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class WheelInteraction implements SlotInteraction {
    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        // 1. SI L'EMPLACEMENT EST VIDE
        if (slot.isEmpty()) {
            if (stackInHand.getItem() instanceof WheelItem wheelItem) {
                if (wheelItem.getPartCategory() != slot.getAllowedCategory()) {
                    if (!vehicle.level().isClientSide())
                        player.displayClientMessage(Component.literal("§cCette roue n'est pas adaptée à ce véhicule !"),
                                true);
                    return InteractionResult.SUCCESS;
                }

                WheelPart newWheel = wheelItem.createPart();
                if (slot.installPart(newWheel)) {
                    if (!player.isCreative())
                        stackInHand.shrink(1);
                    if (!vehicle.level().isClientSide()) {
                        player.displayClientMessage(Component.literal("§aRoue posée ! (Non fixée)"), true);
                        vehicle.updatePartsSync();
                    }
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            } else if (!stackInHand.isEmpty()) {
                if (!vehicle.level().isClientSide())
                    player.displayClientMessage(Component.literal("§cCet emplacement nécessite une Roue !"), true);
                return InteractionResult.SUCCESS;
            }
        }
        // 2. SI LA ROUE EST POSÉE MAIS PAS FIXÉE
        else if (!slot.isSecured()) {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.securePart()) {
                    if (!vehicle.level().isClientSide()) {
                        player.displayClientMessage(Component.literal("§aRoue fixée et prête à l'usage !"), true);
                        vehicle.updatePartsSync();
                    }
                    return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
                }
            }
            // --- LE FIX EST ICI : Ajout de player.isShiftKeyDown() ---
            else if (stackInHand.isEmpty() && player.isShiftKeyDown()) {
                slot.removePart();
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal("§eRoue retirée !"), true);
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
        // 3. SI LA ROUE EST FIXÉE
        else {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.unsecurePart()) {
                    if (!vehicle.level().isClientSide()) {
                        player.displayClientMessage(Component.literal("§eRoue dévissée."), true);
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