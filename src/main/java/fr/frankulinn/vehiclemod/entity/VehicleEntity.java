package fr.frankulinn.vehiclemod.entity;

import fr.frankulinn.vehiclemod.entity.parts.EnginePart;
import fr.frankulinn.vehiclemod.entity.parts.InteractionPartEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.registers.ModEntities;
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
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VehicleEntity extends Entity implements GeoEntity {

    // Ces variables sont synchronisées automatiquement du Serveur vers le Client
    private static final EntityDataAccessor<Boolean> HAS_ENGINE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ENGINE_SECURED = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> SECURED_WHEELS = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);

    private final Map<String, PartSlot> partSlots = new HashMap<>();
    private final List<InteractionPartEntity> hitboxes = new ArrayList<>();
    private boolean hitboxesSpawned = false;

    // Constructeur obligatoire pour que Minecraft puisse spawner l'entité
    public VehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        initSlots();
    }

    private void initSlots() {
        this.partSlots.put("engine_bay", new PartSlot("engine_bay"));
        this.partSlots.put("wheel_front_left", new PartSlot("wheel_front_left"));
        this.partSlots.put("wheel_front_right", new PartSlot("wheel_front_right"));
        this.partSlots.put("wheel_back_left", new PartSlot("wheel_back_left"));
        this.partSlots.put("wheel_back_right", new PartSlot("wheel_back_right"));
    }

    public PartSlot getSlot(String slotId) {
        return this.partSlots.get(slotId);
    }

    private void spawnHitboxes() {
        // Le Moteur
        createHitbox("engine_bay", new Vec3(0, 0.5, 1.0), 0.8f, 0.8f);

        // Les 4 Roues (Largeur X, Hauteur Y, Profondeur Z)
        // L'axe X : Positif = Gauche, Négatif = Droite
        // L'axe Z : Positif = Avant, Négatif = Arrière

        createHitbox("wheel_front_left", new Vec3(1.0, 0.2, 1.0), 0.5f, 0.5f);
        createHitbox("wheel_front_right", new Vec3(-1.0, 0.2, 1.0), 0.5f, 0.5f); // Droite (X négatif)

        createHitbox("wheel_back_left", new Vec3(1.0, 0.2, -1.0), 0.5f, 0.5f); // Arrière (Z négatif)
        createHitbox("wheel_back_right", new Vec3(-1.0, 0.2, -1.0), 0.5f, 0.5f); // Arrière Droite
    }

    private void createHitbox(String slotId, Vec3 offset, float width, float height) {
        InteractionPartEntity hitbox = ModEntities.INTERACTION_PART.get().create(this.level());
        if (hitbox != null) {
            hitbox.init(this, slotId, offset, width, height);
            hitbox.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(hitbox);
            this.hitboxes.add(hitbox);
        }
    }



    // C'est ici qu'on déclarera plus tard nos variables synchronisées (ex: la
    // couleur, isLifted, etc.)
    // En 1.21.1, on utilise le builder.
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // Vide pour l'instant
        builder.define(HAS_ENGINE, false);
        builder.define(ENGINE_SECURED, false);
        builder.define(SECURED_WHEELS, 0); // 0 roue vissée par défaut
    }

    public void updatePartsSync() {
        // 1. Synchro du Moteur
        PartSlot engineSlot = this.getSlot("engine_bay");
        this.entityData.set(HAS_ENGINE, engineSlot != null && engineSlot.getPart() != null);
        this.entityData.set(ENGINE_SECURED, engineSlot != null && engineSlot.isSecured());

        // 2. Synchro des Roues (On compte combien sont vissées)
        int wheelCount = 0;
        if (isWheelSecured("wheel_front_left")) wheelCount++;
        if (isWheelSecured("wheel_front_right")) wheelCount++;
        if (isWheelSecured("wheel_back_left")) wheelCount++;
        if (isWheelSecured("wheel_back_right")) wheelCount++;

        this.entityData.set(SECURED_WHEELS, wheelCount);
    }

    private boolean isWheelSecured(String slotId) {
        PartSlot slot = this.getSlot(slotId);
        return slot != null && slot.isSecured() && slot.getPart() instanceof fr.frankulinn.vehiclemod.entity.parts.WheelPart;
    }

    // Empêche les autres entités (et ses propres composants) de pousser la voiture
    @Override
    public boolean isPushable() {
        return false;
    }

    // Définit avec qui la voiture a le droit d'entrer en collision physique
    @Override
    public boolean canCollideWith(Entity entity) {
        // On ignore totalement la collision si l'entité touchée est un de nos
        // composants
        if (entity instanceof fr.frankulinn.vehiclemod.entity.parts.InteractionPartEntity) {
            return false;
        }
        return super.canCollideWith(entity);
    }

    // === FIX ROLLBACK: On ignore les corrections de position du tracker serveur
    // ===
    // Le tracker d'entité envoie des positions STALE au client via lerpTo().
    // Comme on calcule la physique identiquement côté client ET serveur,
    // le client a déjà la bonne position → on ignore ces corrections.
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        // Ne PAS appeler super.lerpTo() — c'est lui qui causait le rollback
    }

    @Override
    public void tick() {
        super.tick();

        // 1. Apparition des hitboxes (Serveur uniquement)
        if (!this.level().isClientSide() && !this.hitboxesSpawned) {
            this.spawnHitboxes();
            this.hitboxesSpawned = true;
        }

        // 2. Gestion de la Physique et du Réseau
        if (this.isVehicle() && this.getControllingPassenger() instanceof Player driver) {
            this.updateAcceleration(driver);
            this.updateSpeed();
        } else {
            Vec3 current = this.getDeltaMovement();
            double motionY = this.isNoGravity() ? 0.0 : -0.08;
            this.setDeltaMovement(current.x * 0.5, motionY, current.z * 0.5);
            this.updateSpeed();
        }

        // 3. Aligne la rotation visuelle pour le Client et le Serveur
        this.setYBodyRot(this.getYRot());
    }

    protected void updateAcceleration(Player driver) {
        float forwardImpulse = driver.zza;
        float strafeImpulse = -driver.xxa;

        if (forwardImpulse != 0) {
            this.setYRot(this.getYRot() + strafeImpulse * 4.0f);
        }


        double speedMultiplier = 0.0;

        if (this.entityData.get(HAS_ENGINE) && this.entityData.get(ENGINE_SECURED)) {
            speedMultiplier = 150.0 / 250.0;
        }

        // --- NOUVEAU : GESTION DES ROUES ---
        int wheels = this.entityData.get(SECURED_WHEELS);

        // On multiplie la vitesse par le ratio de roues (ex: 2 roues / 4 = 50% de la vitesse)
        // S'il y a 0 roue, 0 / 4 = 0, la voiture n'avance pas !
        speedMultiplier *= (wheels / 4.0);

        Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());

        // L'accélération dépendra maintenant du vrai moteur
        double motionX = forwardVec.x * forwardImpulse * speedMultiplier;
        double motionZ = forwardVec.z * forwardImpulse * speedMultiplier;
        double motionY = this.isNoGravity() ? 0.0 : -0.08;

        this.setDeltaMovement(motionX, motionY, motionZ);
    }

    protected void updateSpeed() {
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
            // On place le joueur au centre de la hitbox, surélevé de 0.5 bloc pour qu'il ne
            // rentre pas dans le sol
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

    // Coupe le son des bruits de pas de l'entité
    @Override
    protected void playStepSound(net.minecraft.core.BlockPos pos, net.minecraft.world.level.block.state.BlockState blockIn) {
        // En laissant cette méthode vide, la voiture devient silencieuse.
        // Plus tard, on mettra le bruit de roulement des pneus sur l'asphalte ici !
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
