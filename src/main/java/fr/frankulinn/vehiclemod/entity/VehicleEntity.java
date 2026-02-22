package fr.frankulinn.vehiclemod.entity;

import fr.frankulinn.vehiclemod.entity.parts.EnginePart;
import fr.frankulinn.vehiclemod.entity.parts.InteractionPartEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.EngineBayInteraction;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.FuelCapInteraction;
import fr.frankulinn.vehiclemod.entity.parts.SlotInteractions.WheelInteraction;
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
import java.util.*;

public class VehicleEntity extends Entity implements GeoEntity {

    // Ces variables sont synchronisées automatiquement du Serveur vers le Client
    public static final EntityDataAccessor<Boolean> HAS_ENGINE = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ENGINE_SECURED = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.BOOLEAN);
    // Avec HAS_ENGINE, WHEEL_FL, etc...
    public static final EntityDataAccessor<Integer> SECURED_WHEELS = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> WHEEL_FL = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WHEEL_FR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WHEEL_BL = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WHEEL_BR = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<Float> FUEL_LEVEL = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);
    public static final float MAX_FUEL = 100.0f; // La capacité maximum du réservoir

    private final Map<String, PartSlot> partSlots = new HashMap<>();
    private final List<InteractionPartEntity> hitboxes = new ArrayList<>();
    private boolean hitboxesSpawned = false;
    public float wheelRotation = 0.0f;
    public float prevWheelRotation = 0.0f;
    public float steeringAngle = 0.0f;
    public float prevSteeringAngle = 0.0f;

    // Constructeur obligatoire pour que Minecraft puisse spawner l'entité
    public VehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        initSlots();
    }

    private void initSlots() {
        // ID du slot | Position (X, Y, Z) | Largeur de la hitbox | Hauteur de la hitbox

        // Le Moteur
        // On passe le Z de 1.0 à -1.0 (ou -1.5 si le moteur est encore plus en arrière !)
        this.partSlots.put("engine_bay", new fr.frankulinn.vehiclemod.entity.parts.PartSlot("engine_bay", new Vec3(0, 0.5, -1.0), 0.8f, 0.8f, new EngineBayInteraction()));
        // Les 4 Roues
        this.partSlots.put("wheel_front_left", new PartSlot("wheel_front_left", new Vec3(1.0, 0.2, 1.0), 0.5f, 0.5f, new WheelInteraction()));
        this.partSlots.put("wheel_front_right", new PartSlot("wheel_front_right", new Vec3(-1.0, 0.2, 1.0), 0.5f, 0.5f, new WheelInteraction()));
        this.partSlots.put("wheel_back_left", new PartSlot("wheel_back_left", new Vec3(1.0, 0.2, -1.0), 0.5f, 0.5f, new WheelInteraction()));
        this.partSlots.put("wheel_back_right", new PartSlot("wheel_back_right", new Vec3(-1.0, 0.2, -1.0), 0.5f, 0.5f, new WheelInteraction()));
        this.partSlots.put("fuel_cap", new fr.frankulinn.vehiclemod.entity.parts.PartSlot("fuel_cap", new Vec3(0.8, 0.5, -1.0), 0.4f, 0.4f, new FuelCapInteraction()));
    }

    private void spawnHitboxes() {
        // On boucle sur tous les slots enregistrés et on génère leur hitbox automatiquement !
        for (fr.frankulinn.vehiclemod.entity.parts.PartSlot slot : this.partSlots.values()) {
            createHitbox(slot.getId(), slot.getOffset(), slot.getHitboxWidth(), slot.getHitboxHeight());
        }
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
        builder.define(SECURED_WHEELS, 0); // 0 roue vissée au départ
        builder.define(WHEEL_FL, "none");
        builder.define(WHEEL_FR, "none");
        builder.define(WHEEL_BL, "none");
        builder.define(WHEEL_BR, "none");
        builder.define(FUEL_LEVEL, 0.0f);
    }

    public void updatePartsSync() {
        // 1. Synchro du Moteur (Présence ET Fixation)
        fr.frankulinn.vehiclemod.entity.parts.PartSlot engineSlot = this.getSlot("engine_bay");
        this.entityData.set(HAS_ENGINE, engineSlot != null && engineSlot.getPart() != null);
        this.entityData.set(ENGINE_SECURED, engineSlot != null && engineSlot.isSecured());

        // 🔥 NOUVEAU : Synchro des types de roues pour l'affichage visuel
        this.entityData.set(WHEEL_FL, getWheelTypeAt("wheel_front_left"));
        this.entityData.set(WHEEL_FR, getWheelTypeAt("wheel_front_right"));
        this.entityData.set(WHEEL_BL, getWheelTypeAt("wheel_back_left"));
        this.entityData.set(WHEEL_BR, getWheelTypeAt("wheel_back_right"));

        // 2. Compte des roues pour la vitesse
        int count = 0;
        if (this.getSlot("wheel_front_left") != null && this.getSlot("wheel_front_left").isSecured()) count++;
        if (this.getSlot("wheel_front_right") != null && this.getSlot("wheel_front_right").isSecured()) count++;
        if (this.getSlot("wheel_back_left") != null && this.getSlot("wheel_back_left").isSecured()) count++;
        if (this.getSlot("wheel_back_right") != null && this.getSlot("wheel_back_right").isSecured()) count++;

        this.entityData.set(SECURED_WHEELS, count);
    }

    private String getWheelTypeAt(String slotId) {
        PartSlot slot = this.getSlot(slotId);
        if (slot != null && slot.getPart() instanceof fr.frankulinn.vehiclemod.entity.parts.WheelPart wheel) {
            return wheel.getWheelType();
        }
        return "none";
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

        // 3. Aligne la rotation visuelle
        this.setYBodyRot(this.getYRot());

        // --- NOUVEAU : 4. ANIMATION DES ROUES (Client uniquement) ---
        if (this.level().isClientSide()) {
            this.prevWheelRotation = this.wheelRotation;
            this.prevSteeringAngle = this.steeringAngle;

            // Vitesse réelle de déplacement horizontal
            double speed = this.getDeltaMovement().horizontalDistance();

            if (speed > 0.01) {
                // On vérifie si la voiture avance ou recule par rapport à sa direction
                Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());
                boolean isReversing = this.getDeltaMovement().dot(forwardVec) < 0;

                // Formule mathématique : Vitesse / Rayon de la roue.
                // Le "4.0f" dépend de la taille de ta roue. Si elle tourne trop lentement, augmente-le !
                float rotationSpeed = (float) speed * 4.0f;
                this.wheelRotation += isReversing ? -rotationSpeed : rotationSpeed;
            }

            // Calcul du braquage (volant)
            if (this.getControllingPassenger() instanceof Player driver) {
                // xxa représente les touches Q/D (ou A/D). On multiplie par 35 degrés max.
                float targetSteering = -driver.xxa * 35.0f;

                // On lisse le mouvement avec un Lerp pour que les roues ne tournent pas de façon saccadée
                this.steeringAngle += (targetSteering - this.steeringAngle) * 0.2f;
            } else {
                // Si personne ne conduit, les roues se remettent droites doucement
                this.steeringAngle += (0.0f - this.steeringAngle) * 0.1f;
            }
        }
    }

    protected void updateAcceleration(Player driver) {
        float forwardImpulse = driver.zza;
        float strafeImpulse = -driver.xxa;

        double speedMultiplier = 0.0;
        float currentFuel = this.entityData.get(FUEL_LEVEL);

        // 1. On calcule d'abord si la voiture PEUT avancer
        if (this.entityData.get(HAS_ENGINE) && this.entityData.get(ENGINE_SECURED) && currentFuel > 0) {
            speedMultiplier = 150.0 / 250.0;

            // On ne consomme de l'essence que si on appuie vraiment sur l'accélérateur
            if (forwardImpulse != 0 && !this.level().isClientSide()) {
                float newFuel = Math.max(0.0f, currentFuel - 0.05f);
                this.entityData.set(FUEL_LEVEL, newFuel);
            }
        }

        // On ajuste selon le nombre de roues
        int securedWheels = this.entityData.get(SECURED_WHEELS);
        speedMultiplier *= (securedWheels / 4.0);

        // --- CORRECTION ICI : On tourne uniquement si on appuie sur Z/S ET que le kart peut rouler ! ---
        if (forwardImpulse != 0 && speedMultiplier > 0) {
            this.setYRot(this.getYRot() + strafeImpulse * 4.0f);
        }

        // 2. On applique le mouvement
        Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());

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
    protected void addAdditionalSaveData(CompoundTag compound) {
        CompoundTag slotsTag = new CompoundTag();

        // On sauvegarde chaque emplacement par son nom ("engine_bay", etc.)
        for (java.util.Map.Entry<String, fr.frankulinn.vehiclemod.entity.parts.PartSlot> entry : this.partSlots.entrySet()) {
            slotsTag.put(entry.getKey(), entry.getValue().save());
        }

        compound.put("PartSlots", slotsTag);
        compound.putFloat("FuelLevel", this.entityData.get(FUEL_LEVEL));
    }

    // Chargement des données quand on rejoint le monde
    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("PartSlots")) {
            CompoundTag slotsTag = compound.getCompound("PartSlots");

            for (String slotId : slotsTag.getAllKeys()) {
                fr.frankulinn.vehiclemod.entity.parts.PartSlot slot = this.getSlot(slotId);
                if (slot != null) {
                    // On demande à l'emplacement de se recharger avec les données lues
                    slot.load(slotsTag.getCompound(slotId));
                }
            }
        }

        if (compound.contains("FuelLevel")) {
            this.entityData.set(FUEL_LEVEL, compound.getFloat("FuelLevel"));
        }

        // 🔥 CRUCIAL : Une fois que le serveur a rechargé les pièces depuis le fichier,
        // on l'oblige à mettre à jour le réseau (SynchedEntityData) pour que le Client
        // (ton écran) affiche les modèles 3D et calcule la bonne vitesse !
        this.updatePartsSync();
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

    public PartSlot getSlot(String slotId) {
        return this.partSlots.get(slotId);
    }

    public Collection<PartSlot> getPartSlots() {
        return this.partSlots.values();
    }



}
