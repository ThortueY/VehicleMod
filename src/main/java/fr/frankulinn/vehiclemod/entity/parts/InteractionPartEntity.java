package fr.frankulinn.vehiclemod.entity.parts;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class InteractionPartEntity extends Entity {

    private VehicleEntity parentVehicle;
    private String slotId;
    private Vec3 offset = Vec3.ZERO;

    // Constructeur obligatoire pour le registre NeoForge
    public InteractionPartEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // Empêche cette hitbox de taper les murs
    }

    // Notre méthode pour lier la hitbox à la voiture juste après l'avoir fait
    // spawner
    public void init(VehicleEntity parentVehicle, String slotId, Vec3 offset, float width, float height) {
        this.parentVehicle = parentVehicle;
        this.slotId = slotId;
        this.offset = offset;
        this.setBoundingBox(new AABB(0, 0, 0, width, height, width));
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide()) {
            if (this.parentVehicle == null || this.parentVehicle.isRemoved()) {
                this.discard();
                return;
            }
            // Suivi parfait de la voiture
            Vec3 rotatedOffset = this.offset.yRot(-this.parentVehicle.getYRot() * ((float) Math.PI / 180F));
            this.setPos(this.parentVehicle.getX() + rotatedOffset.x, this.parentVehicle.getY() + rotatedOffset.y,
                    this.parentVehicle.getZ() + rotatedOffset.z);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // Empêche la hitbox de pousser le châssis parent
    @Override
    public boolean canCollideWith(Entity entity) {
        if (entity == this.parentVehicle) {
            return false;
        }
        return super.canCollideWith(entity);
    }

    @Override
    public boolean isPickable() {
        return true; // Obligatoire pour pouvoir cliquer dessus avec la souris !
    }

    // Méthodes obligatoires vides pour l'instant
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }
}
