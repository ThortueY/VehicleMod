package fr.frankulinn.vehiclemod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;

public class VehicleEntity extends Entity implements GeoEntity {

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

    @Override
    public void tick() {
        super.tick();

        // 1. Calcul de la force, lecture des touches et de la gravité
        this.updateAcceleration();

        // 2. Application de la vitesse et gestion des collisions avec le décor
        this.updateSpeed();
    }

    protected void updateAcceleration() {
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player driver) {

            // On donne l'autorité totale au CLIENT (celui qui a le clavier)
            if (this.level().isClientSide()) {
                float forwardImpulse = driver.zza;
                float strafeImpulse = -driver.xxa;

                if (forwardImpulse != 0) {
                    this.setYRot(this.getYRot() + strafeImpulse * 4.0f);
                }

                double speedMultiplier = 0.6;
                Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());

                double motionX = forwardVec.x * forwardImpulse * speedMultiplier;
                double motionZ = forwardVec.z * forwardImpulse * speedMultiplier;
                double motionY = this.isNoGravity() ? 0.0 : -0.08;

                this.setDeltaMovement(motionX, motionY, motionZ);
            }
            // Si on est sur le Serveur, on ne fait RIEN.
            // On laisse le jeu synchroniser la position automatiquement grâce au paquet de mouvement natif de Minecraft.

        } else {
            // S'il n'y a pas de conducteur, Client ET Serveur appliquent la friction
            Vec3 current = this.getDeltaMovement();
            double motionY = this.isNoGravity() ? 0.0 : -0.08;
            this.setDeltaMovement(current.x * 0.5, motionY, current.z * 0.5);
        }
    }

    protected void updateSpeed() {
        // Le MoverType.SELF gère automatiquement le fait de buter contre un mur
        // ou de monter automatiquement un bloc (step height) si on l'active plus tard
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
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

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        // Si on est sur le serveur et que le joueur n'est pas déjà dans un véhicule
        if (!this.level().isClientSide() && player.getVehicle() == null) {
            // Le joueur monte dans la hitbox
            player.startRiding(this);
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        super.positionRider(passenger, callback);
        if (this.hasPassenger(passenger)) {
            // On place le joueur au centre de la hitbox, surélevé de 0.5 bloc pour qu'il ne rentre pas dans le sol
            callback.accept(passenger, this.getX(), this.getY() + 0.5, this.getZ());
        }
    }

    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        // Le jeu a besoin de savoir qui conduit. C'est le premier passager.
        Entity entity = this.getFirstPassenger();
        return entity instanceof LivingEntity living ? living : null;
    }

    // Le cache obligatoire pour GeckoLib
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // ... (Ton constructeur et tes méthodes tick() restent identiques) ...

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // C'est ici qu'on mettra les animations (roues qui tournent, volant) plus tard.
        // Pour l'instant, on laisse vide, on veut juste un modèle statique.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
