package fr.frankulinn.vehiclemod.registers;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.item.EngineItem;
import fr.frankulinn.vehiclemod.item.WheelItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;



public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Vehiclemod.MODID);


    // Remplace "new Item" par "new EngineItem" (ou le nom exact de ta classe)
    public static final DeferredItem<Item> ENGINE_ITEM = ITEMS.register("engine", () -> new EngineItem(new Item.Properties()));

    public static final DeferredItem<Item> WRENCH_ITEM = ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties()));

    public static final DeferredItem<Item> WHEEL_ITEM = ITEMS.register("wheel", () -> new WheelItem(new Item.Properties()));


}

