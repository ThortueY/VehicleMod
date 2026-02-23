package fr.frankulinn.vehiclemod.entity.parts;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class InteractionPartEntity extends Entity {

    private BaseVehicleEntity parentVehicle;
    private String slotId;
    private Vec3 offset = Vec3.ZERO;
    private float customWidth = 0.5f;
    private float customHeight = 0.5f;
    private long lastInteractTime = 0;

    // Constructeur obligatoire pour le registre NeoForge
    public InteractionPartEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true; // Empêche cette hitbox de taper les murs
    }

    // Notre méthode pour lier la hitbox à la voiture juste après l'avoir fait
    // spawner
    public void init(BaseVehicleEntity parentVehicle, String slotId, Vec3 offset, float width, float height) {
        this.parentVehicle = parentVehicle;
        this.slotId = slotId;
        this.offset = offset;
        this.customWidth = width;
        this.customHeight = height;
        // On force Minecraft à recalculer la boîte de collision de l'entité avec NOS
        // tailles !
        this.refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        // "fixed" empêche Minecraft de redimensionner la hitbox tout seul
        return EntityDimensions.fixed(this.customWidth, this.customHeight);
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
    public InteractionResult interact(Player player, InteractionHand hand) {
        long currentTime = this.level().getGameTime();

        if (currentTime - this.lastInteractTime < 5) {
            return InteractionResult.PASS;
        }

        this.lastInteractTime = currentTime;

        if (this.parentVehicle == null)
            return InteractionResult.PASS;

        PartSlot slot = this.parentVehicle.getSlot(this.slotId);
        if (slot == null)
            return InteractionResult.PASS;

        // 🔥 LA MAGIE DU POLYMORPHISME 🔥
        // L'entité ne sait pas ce qu'elle est. Elle demande juste au comportement de
        // s'exécuter.
        if (slot.getInteractionBehavior() != null) {
            return slot.getInteractionBehavior().onInteract(player, hand, slot, this.parentVehicle);
        }

        return InteractionResult.PASS;
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
