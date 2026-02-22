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
            if (stackInHand.getItem() instanceof WheelItem) {
                WheelPart newWheel = new WheelPart(1.0f, 15.0f, "kart_wheel");
                if (slot.installPart(newWheel)) {
                    if (!player.isCreative()) stackInHand.shrink(1);
                    player.displayClientMessage(Component.literal("§aRoue posée ! (Non fixée)"), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.CONSUME;
                }
            } else if (!stackInHand.isEmpty()) { // N'affiche le message d'erreur que si on tient un objet
                player.displayClientMessage(Component.literal("§cCet emplacement nécessite une Roue !"), true);
            }
        }
        // 2. SI LA ROUE EST POSÉE MAIS PAS FIXÉE
        else if (!slot.isSecured()) {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.securePart()) {
                    player.displayClientMessage(Component.literal("§aPièce fixée et prête à l'usage !"), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.SUCCESS;
                }
            } else if (stackInHand.isEmpty()) {
                slot.removePart();
                // (Ici tu pourras ajouter le code pour redonner l'item Roue au joueur plus tard)
                player.displayClientMessage(Component.literal("§ePièce retirée !"), true);
                vehicle.updatePartsSync();
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal("§cUtilisez une Clé pour fixer, ou les mains vides pour enlever."), true);
            }
        }
        // 3. SI LA ROUE EST FIXÉE
        else {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.unsecurePart()) {
                    player.displayClientMessage(Component.literal("§ePièce dévissée."), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.SUCCESS;
                }
            } else {
                player.displayClientMessage(Component.literal("§cCette pièce est vissée ! Utilisez une Clé."), true);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
