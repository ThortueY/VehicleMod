package fr.frankulinn.vehiclemod.entity.vehicles;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartCategory;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.EngineBayInteraction;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.FuelCapInteraction;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.SeatInteraction;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.WheelInteraction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class KartEntity extends BaseVehicleEntity {

    public KartEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void initSlots() {
        // Le Moteur
        this.addSlot("engine_bay", new PartSlot("engine_bay", new Vec3(0, 0.5, -1.0), 0.8f, 0.8f,
                new EngineBayInteraction(), PartCategory.KART_ENGINE));
        // Les 4 Roues
        this.addSlot("wheel_front_left", new PartSlot("wheel_front_left", new Vec3(1.0, 0.2, 1.0), 0.5f, 0.5f,
                new WheelInteraction(), PartCategory.KART_WHEEL));
        this.addSlot("wheel_front_right", new PartSlot("wheel_front_right", new Vec3(-1.0, 0.2, 1.0), 0.5f, 0.5f,
                new WheelInteraction(), PartCategory.KART_WHEEL));
        this.addSlot("wheel_back_left", new PartSlot("wheel_back_left", new Vec3(1.0, 0.2, -1.0), 0.5f, 0.5f,
                new WheelInteraction(), PartCategory.KART_WHEEL));
        this.addSlot("wheel_back_right", new PartSlot("wheel_back_right", new Vec3(-1.0, 0.2, -1.0), 0.5f, 0.5f,
                new WheelInteraction(), PartCategory.KART_WHEEL));

        // La Trappe à Essence
        this.addSlot("fuel_cap", new PartSlot("fuel_cap", new Vec3(0.8, 0.5, -1.0), 0.4f, 0.4f,
                new FuelCapInteraction(), PartCategory.FUEL_CAP));

        this.addSlot("seat_driver", new PartSlot("seat_driver", new Vec3(0.0, 0.2, -0.2), 0.6f, 0.6f,
                new SeatInteraction(), PartCategory.GENERIC_SEAT));
    }
}
