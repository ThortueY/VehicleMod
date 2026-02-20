package fr.frankulinn.vehiclemod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class VehicleEntity extends Entity {

    // Constructeur obligatoire pour que Minecraft puisse spawner l'entité
    public VehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    // C'est ici qu'on déclarera plus tard nos variables synchronisées (ex: la couleur, isLifted, etc.)
    // En 1.21.1, on utilise le builder.
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Vide pour l'instant
    }

    // La boucle principale de notre véhicule (appelée 20 fois par seconde)
    @Override
    public void tick() {
        super.tick();

        // Pour l'instant, on applique juste la gravité de base pour que la boîte tombe au sol
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
        }

        // Applique le mouvement à la hitbox (collisions de base avec le monde)
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());

        // Ralentissement basique (Friction de l'air/sol) pour éviter qu'il glisse à l'infini
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
    }

    // Sauvegarde des données quand on quitte le monde
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        // On lira l'essence, les pièces, etc. ici plus tard
    }

    // Chargement des données quand on rejoint le monde
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        // On sauvegardera l'essence, les pièces, etc. ici plus tard
    }

    // Empêche le véhicule de despawner comme un simple zombie
    @Override
    public boolean isPickable() {
        return true; // Permet aux joueurs de cliquer sur la hitbox
    }
}
