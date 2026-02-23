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

    /**
     * Fonction gérant l'évènement de sécurisation d'un siège
     * @param player le joueur concerné par l'évènement
     * @param hand la main du joueur
     * @param slot le slot visé par la sécurisation
     * @param vehicle le véhicule visé par la sécurisation
     * @return le résultat de l'évènement
     */
    public InteractionResult onSecuringPart(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        if (slot.securePart()) {
            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§aSiège fixé et prêt à l'usage !"), true);
                vehicle.updatePartsSync();
            }
            return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
        }

        return InteractionResult.PASS;
    }

    /**
     * Fonction gérant l'évènement d'installation d'un siège
     * @param player le joueur concerné par l'évènement
     * @param hand la main du joueur
     * @param slot le slot visé par l'installation
     * @param vehicle le véhicule visé par l'installation
     * @return le résultat de l'évènement
     */
    public InteractionResult onInstallingPart(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        if (stackInHand.getItem() instanceof SeatItem seatItem) {

            //Si le siège n'appartient pas à la catégorie de siège accepté par le slot
            if (seatItem.getCategory() != slot.getAllowedCategory()) {
                if (!vehicle.level().isClientSide())
                    player.displayClientMessage(Component.literal("§cCe siège n'est pas adapté à ce véhicule !"),
                            true);
                return InteractionResult.SUCCESS;
            }

            //Sinon, on installe le siège
            SeatPart newSeat = seatItem.createPart();
            if (slot.installPart(newSeat)) {
                if (!player.isCreative())
                    stackInHand.shrink(1);
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal("§aSiège posé ! (Non fixé)"), true);
                    vehicle.updatePartsSync();
                }
                return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
            }

            //Si l'item du joueur n'est pas un siège
        } else if (!stackInHand.isEmpty()) {
            if (!vehicle.level().isClientSide())
                player.displayClientMessage(Component.literal("§cCet emplacement nécessite un Siège !"), true);
                return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /**
     * Fonction gérant l'évènement de retrait d'un siège
     * @param player le joueur concerné par l'évènement
     * @param hand la main du joueur
     * @param slot le slot visé par le retrait
     * @param vehicle le véhicule visé par le retrait
     * @return le résultat de l'évènement
     */
    public InteractionResult onRemovingPart(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        slot.removePart();
        if (!vehicle.level().isClientSide()) {
            player.displayClientMessage(Component.literal("§eSiège retiré !"), true);
            vehicle.updatePartsSync();
        }
        return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
    }

    public InteractionResult onPartUnSecured(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        if (slot.unsecurePart()) {
            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§eSiège dévissé."), true);
                vehicle.updatePartsSync();
            }
            return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
        }

        return InteractionResult.PASS;
    }

    public InteractionResult onPlayerStartRiding(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
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




    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        /*Condition pour s'assoir sur le siège:
        - Il y a une pièce de posé
        - La pièce posé doit être fixé
        - Le joueur ne doit pas avoir d'outil (Wrench) dans la main
         */


        //Actions de sécurisation de la pièce
        if (stackInHand.getItem() instanceof WrenchItem) {
            if (!slot.isSecured() && !slot.isEmpty()) {
                return onSecuringPart(player, hand, slot, vehicle);
            } else {
                return onPartUnSecured(player, hand, slot, vehicle);
            }
        }

        //Actions de montage et démontage de la pièces
        if (slot.isEmpty()) {
            return onInstallingPart(player, hand, slot, vehicle);

        } else {
            System.out.println("Slot not empty there is: " + slot.getPart().getId());
            if (!slot.isSecured()) {
                return onRemovingPart(player, hand, slot, vehicle);
            } else {
                //Dans les autres cas, le composant ne peut pas être retiré du slot car la pièce est sécurisé
                player.displayClientMessage(Component.literal("§cCette pièce est vissée ! Utilisez une Clé."), true);
            }
        }

        //Action pour monter sur un siège
        if (!slot.isEmpty() && slot.isSecured()) {
            if (stackInHand.getItem() instanceof WrenchItem) return InteractionResult.PASS;
            else return onPlayerStartRiding(player, hand, slot, vehicle);
        }

        return InteractionResult.PASS;
    }
}
