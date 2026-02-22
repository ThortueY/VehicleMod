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
            if (stackInHand.getItem() instanceof EngineItem) {
                EnginePart newEngine = new EnginePart(150.0f, 150.0f, 30.0f , 0.05f);
                if (slot.installPart(newEngine)) {
                    if (!player.isCreative()) stackInHand.shrink(1);
                    player.displayClientMessage(Component.literal("§aMoteur posé ! (Non fixé)"), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.CONSUME;
                }
            } else if (!stackInHand.isEmpty()) {
                player.displayClientMessage(Component.literal("§cCet emplacement nécessite un Moteur !"), true);
            }
        }
        // 2. SI LE MOTEUR EST POSÉ MAIS PAS FIXÉ
        else if (!slot.isSecured()) {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.securePart()) {
                    player.displayClientMessage(Component.literal("§aPièce fixée et prête à l'usage !"), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.SUCCESS;
                }
            } else if (stackInHand.isEmpty()) {
                slot.removePart();
                player.displayClientMessage(Component.literal("§ePièce retirée !"), true);
                vehicle.updatePartsSync();
                return InteractionResult.SUCCESS;
            } else {
                player.displayClientMessage(Component.literal("§cUtilisez une Clé pour fixer, ou les mains vides pour enlever."), true);
            }
        }
        // 3. SI LE MOTEUR EST FIXÉ
        else {
            if (stackInHand.getItem() instanceof WrenchItem) {
                if (slot.unsecurePart()) {
                    player.displayClientMessage(Component.literal("§ePièce dévissée."), true);
                    vehicle.updatePartsSync();
                    return InteractionResult.SUCCESS;
                }
            } else {
                // (Si tu as aussi gardé le système du Jerrican sur le moteur, c'est ici qu'il faudrait l'ajouter !)
                player.displayClientMessage(Component.literal("§cCette pièce est vissée ! Utilisez une Clé."), true);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
