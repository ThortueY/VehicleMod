package fr.frankulinn.vehiclemod.registers;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import fr.frankulinn.vehiclemod.item.*;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;



public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Vehiclemod.MODID);



    public static final DeferredItem<Item> WRENCH_ITEM = ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties()));

    public static final DeferredItem<Item> JERRICAN_ITEM = ITEMS.register("jerrican", () -> new JerricanItem(new Item.Properties()));

    //Engines
    public static final DeferredItem<Item> KART_ENGINE = ITEMS.register("kart_engine",
            () -> new EngineItem(new Item.Properties(),PartCategory.KART_ENGINE , 150.0f, 50.0f, 30.0f, 0.05f));


    //Wheels
    public static final DeferredItem<Item> KART_WHEEL = ITEMS.register("kart_wheel",
            () -> new WheelItem(new Item.Properties(), PartCategory.KART_WHEEL ,0.85f, 15.0f));

    //Seats
    public static final DeferredItem<Item> LAWN_MOWER_SEAT = ITEMS.register("lawn_mower_seat",
            () -> new SeatItem(new Item.Properties(),1.5f, PartCategory.GENERIC_SEAT));


}

