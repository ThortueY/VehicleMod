package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.WheelPart;
import fr.frankulinn.vehiclemod.item.WheelItem;
import net.minecraft.world.item.ItemStack;

public class WheelInteraction extends AbstractSlotInteraction<WheelItem, WheelPart> {

    @Override
    protected boolean isValidItem(ItemStack stack) {
        return stack.getItem() instanceof WheelItem;
    }

    @Override
    protected boolean isCategoryValid(WheelItem item, PartSlot slot) {
        return item.getPartCategory() == slot.getAllowedCategory();
    }

    @Override
    protected WheelPart createPart(WheelItem item) {
        return item.createPart();
    }

    @Override
    protected String getPartName() {
        return "Roue";
    }

    @Override
    protected String getInvalidCategoryMessage() {
        return "§cCette roue n'est pas adaptée à ce véhicule !";
    }

    @Override
    protected String getPlacedMessage() {
        return "§aRoue posée ! (Non fixée)";
    }

    @Override
    protected String getSecuredMessage() {
        return "§aRoue fixée et prête à l'usage !";
    }

    @Override
    protected String getRemovedMessage() {
        return "§eRoue retirée !";
    }

    @Override
    protected String getUnsecuredMessage() {
        return "§eRoue dévissée.";
    }
}