package fr.frankulinn.vehiclemod.entity.parts.SlotInteractions;

import fr.frankulinn.vehiclemod.entity.parts.EnginePart;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.item.EngineItem;
import net.minecraft.world.item.ItemStack;

public class EngineBayInteraction extends AbstractSlotInteraction<EngineItem, EnginePart> {

    @Override
    protected boolean isValidItem(ItemStack stack) {
        return stack.getItem() instanceof EngineItem;
    }

    @Override
    protected boolean isCategoryValid(EngineItem item, PartSlot slot) {
        return item.getPartCategory() == slot.getAllowedCategory();
    }

    @Override
    protected EnginePart createPart(EngineItem item) {
        return item.createPart();
    }

    @Override
    protected String getPartName() {
        return "Moteur";
    }

    @Override
    protected String getInvalidCategoryMessage() {
        return "§cCe moteur n'est pas adapté à ce véhicule !";
    }
}