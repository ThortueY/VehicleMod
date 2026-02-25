package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteraction;
import fr.frankulinn.vehiclemod.entity.parts.VehiclePart;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public abstract class AbstractSlotInteraction<T extends Item, P extends VehiclePart> implements SlotInteraction {

    protected abstract boolean isValidItem(ItemStack stack);

    protected abstract boolean isCategoryValid(T item, PartSlot slot);

    protected abstract P createPart(T item);

    protected abstract String getPartName();

    protected String getInvalidCategoryMessage() {
        return "§cCette pièce n'est pas adaptée à ce véhicule !";
    }

    protected String getPlacedMessage() {
        return "§a" + getPartName() + " posé ! (Non fixé)";
    }

    protected String getMissingItemMessage() {
        return "§cCet emplacement nécessite un(e) " + getPartName() + " !";
    }

    protected String getSecuredMessage() {
        return "§a" + getPartName() + " fixé et prêt à l'usage !";
    }

    protected String getRemovedMessage() {
        return "§e" + getPartName() + " retiré !";
    }

    protected String getUnsecuredMessage() {
        return "§e" + getPartName() + " dévissé.";
    }

    @Override
    public InteractionResult onInteract(Player player, InteractionHand hand, PartSlot slot, BaseVehicleEntity vehicle) {
        ItemStack stackInHand = player.getItemInHand(hand);

        // 1. SI L'EMPLACEMENT EST VIDE
        if (slot.isEmpty()) {
            return handleEmptySlot(player, stackInHand, slot, vehicle);
        }
        // 2. SI LA PIÈCE EST POSÉE MAIS PAS FIXÉE
        else if (!slot.isSecured()) {
            return handlePlacedSlot(player, stackInHand, slot, vehicle);
        }
        // 3. SI LA PIÈCE EST FIXÉE
        else {
            return handleSecuredSlot(player, hand, stackInHand, slot, vehicle);
        }
    }

    protected InteractionResult handleEmptySlot(Player player, ItemStack stackInHand, PartSlot slot,
            BaseVehicleEntity vehicle) {
        if (isValidItem(stackInHand)) {
            T item = (T) stackInHand.getItem();
            if (!isCategoryValid(item, slot)) {
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(getInvalidCategoryMessage()), true);
                }
                return InteractionResult.SUCCESS;
            }

            P newPart = createPart(item);
            if (slot.installPart(newPart)) {
                if (!player.isCreative()) {
                    stackInHand.shrink(1);
                }
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(getPlacedMessage()), true);
                    vehicle.updatePartsSync();
                }
                return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
            }
        } else if (!stackInHand.isEmpty()) {
            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(Component.literal(getMissingItemMessage()), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult handlePlacedSlot(Player player, ItemStack stackInHand, PartSlot slot,
            BaseVehicleEntity vehicle) {
        if (stackInHand.getItem() instanceof WrenchItem) {
            if (slot.securePart()) {
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(getSecuredMessage()), true);
                    vehicle.updatePartsSync();
                }
                return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
            }
        } else if (stackInHand.isEmpty() && player.isShiftKeyDown()) {
            slot.removePart();
            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(Component.literal(getRemovedMessage()), true);
                vehicle.updatePartsSync();
            }
            return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
        } else {
            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(
                        Component.literal("§cUtilisez une Clé pour fixer, ou Shift + Mains vides pour enlever."), true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    protected InteractionResult handleSecuredSlot(Player player, InteractionHand hand, ItemStack stackInHand,
            PartSlot slot, BaseVehicleEntity vehicle) {
        if (stackInHand.getItem() instanceof WrenchItem) {
            if (slot.unsecurePart()) {
                if (!vehicle.level().isClientSide()) {
                    player.displayClientMessage(Component.literal(getUnsecuredMessage()), true);
                    vehicle.updatePartsSync();
                }
                return InteractionResult.sidedSuccess(vehicle.level().isClientSide());
            }
            return InteractionResult.PASS;
        } else {
            // Action personnalisée quand la pièce est sécurisée (ex: s'assoir)
            InteractionResult customResult = onSecuredInteract(player, hand, slot, vehicle);
            if (customResult != InteractionResult.PASS) {
                return customResult;
            }

            if (!vehicle.level().isClientSide()) {
                player.displayClientMessage(Component.literal("§cCette pièce est vissée ! Utilisez une Clé."), true);
            }
            return InteractionResult.SUCCESS;
        }
    }

    // A override si on veut une action spéciale une fois fixée (ex:
    // SeatInteraction)
    protected InteractionResult onSecuredInteract(Player player, InteractionHand hand, PartSlot slot,
            BaseVehicleEntity vehicle) {
        return InteractionResult.PASS;
    }
}
