package fr.frankulinn.vehiclemod.registers;

import fr.frankulinn.vehiclemod.Vehiclemod;
import fr.frankulinn.vehiclemod.entity.parts.InteractionPartEntity;
import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.vehicles.KartEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities {

    // Création du registre pour les types d'entités
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Vehiclemod.MODID);

    //Slot d'intéraction
    public static final Supplier<EntityType<InteractionPartEntity>> INTERACTION_PART = ENTITY_TYPES.register("interaction_part",
            () -> EntityType.Builder.<InteractionPartEntity>of(InteractionPartEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F) // Taille par défaut
                    .clientTrackingRange(10)
                    .build("interaction_part")
    );

    //Enregistrement des véhicules
    public static final Supplier<EntityType<BaseVehicleEntity>> GO_KART = ENTITY_TYPES.register("go_kart",
            () -> EntityType.Builder.<BaseVehicleEntity>of(KartEntity::new, MobCategory.MISC)
                    .sized(2.0F, 1.5F) // Taille de la hitbox (Largeur, Hauteur) en blocs
                    .clientTrackingRange(10) // Distance de rendu
                    .build("go_kart")
    );


}
