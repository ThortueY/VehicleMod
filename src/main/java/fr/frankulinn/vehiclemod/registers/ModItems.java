package fr.frankulinn.vehiclemod.registers;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import fr.frankulinn.vehiclemod.item.EngineItem;
import fr.frankulinn.vehiclemod.item.JerricanItem;
import fr.frankulinn.vehiclemod.item.WheelItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;



public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Vehiclemod.MODID);


    // Remplace "new Item" par "new EngineItem" (ou le nom exact de ta classe)
    public static final DeferredItem<Item> ENGINE_ITEM = ITEMS.register("engine", () -> new EngineItem(new Item.Properties(), PartCategory.KART_ENGINE));

    public static final DeferredItem<Item> WRENCH_ITEM = ITEMS.register("wrench", () -> new WrenchItem(new Item.Properties()));

    public static final DeferredItem<Item> JERRICAN_ITEM = ITEMS.register("jerrican", () -> new JerricanItem(new Item.Properties()));

    public static final DeferredItem<Item> KART_WHEEL = ITEMS.register("kart_wheel",
            () -> new WheelItem(new Item.Properties(), PartCategory.KART_WHEEL));

    // La mauvaise roue pour le test (Roue de Camion)
    public static final DeferredItem<Item> TRUCK_WHEEL = ITEMS.register("truck_wheel",
            () -> new WheelItem(new Item.Properties(), PartCategory.TRUCK_WHEEL));

    // --- LES MOTEURS ---
    // Le bon moteur pour le kart
    public static final DeferredItem<Item> KART_ENGINE = ITEMS.register("kart_engine",
            () -> new EngineItem(new Item.Properties(), PartCategory.KART_ENGINE));

    // Le mauvais moteur pour le test (Moteur de Voiture)
    public static final DeferredItem<Item> CAR_ENGINE = ITEMS.register("car_engine",
            () -> new EngineItem(new Item.Properties(), PartCategory.CAR_ENGINE));



}

