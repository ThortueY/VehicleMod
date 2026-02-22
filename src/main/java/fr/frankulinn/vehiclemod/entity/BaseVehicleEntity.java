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

public abstract class BaseVehicleEntity extends Entity implements GeoEntity {

    //Variables pour vérifier si un moteur est monté et vissé
    public static final EntityDataAccessor<Boolean> HAS_ENGINE = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ENGINE_SECURED = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.BOOLEAN);

    //Variables pour vérifier si les roues sont montées et vissées
    public static final EntityDataAccessor<Integer> SECURED_WHEELS = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> WHEEL_FL = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WHEEL_FR = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WHEEL_BL = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> WHEEL_BR = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.STRING);

    //Variables pour le réservoir
    public static final EntityDataAccessor<Float> FUEL_LEVEL = SynchedEntityData.defineId(BaseVehicleEntity.class, EntityDataSerializers.FLOAT);
    public static final float MAX_FUEL = 100.0f; // La capacité maximum du réservoir

    //Slots pour les pièces de véhicule
    private final Map<String, PartSlot> partSlots = new HashMap<>();
    private final List<InteractionPartEntity> hitboxes = new ArrayList<>();
    private boolean hitboxesSpawned = false;

    //Variables pour la physique du véhicule
    public float wheelRotation = 0.0f;
    public float prevWheelRotation = 0.0f;
    public float steeringAngle = 0.0f;
    public float prevSteeringAngle = 0.0f;

    // Constructeur obligatoire pour que Minecraft puisse spawner l'entité
    public BaseVehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        initSlots();
    }

    //Constructeur permettant de créer les slots
    protected abstract void initSlots();

    //Méthode pour ajouter un slot
    protected void addSlot(String id, PartSlot slot) {
        this.partSlots.put(id, slot);
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
        float forwardImpulse = driver.zza; // Z = Avancer/Reculer
        float strafeImpulse = -driver.xxa; // X = Gauche/Droite

        double enginePower = 0.0;
        float currentFuel = this.entityData.get(FUEL_LEVEL);

        // 1. Calcul de la puissance du moteur
        if (this.entityData.get(HAS_ENGINE) && this.entityData.get(ENGINE_SECURED) && currentFuel > 0) {
            enginePower = 0.05;

            if (forwardImpulse != 0 && !this.level().isClientSide()) {

                // --- NOUVEAU : On récupère la consommation dynamique ---
                float consumptionRate = 0.05f; // Valeur de secours par défaut

                PartSlot engineSlot = this.getSlot("engine_bay");
                if (engineSlot != null && engineSlot.getPart() instanceof EnginePart enginePart) {
                    consumptionRate = enginePart.getFuelConsumption();
                }
                // -------------------------------------------------------

                // On utilise la vraie consommation du moteur !
                float newFuel = Math.max(0.0f, currentFuel - consumptionRate);
                this.entityData.set(FUEL_LEVEL, newFuel);
            }
        }

        // Ajustement selon le nombre de roues
        int securedWheels = this.entityData.get(SECURED_WHEELS);
        enginePower *= (securedWheels / 4.0);

        // 2. On tourne UNIQUEMENT si le kart est en train de rouler (Vitesse > 0.05)
        Vec3 currentMotion = this.getDeltaMovement();
        double horizontalSpeed = currentMotion.horizontalDistance();

        if (horizontalSpeed > 0.05) {
            // Plus on va vite, plus le volant est réactif (tu pourras ajuster ce multiplicateur)
            float turnSpeed = strafeImpulse * 4.0f;

            // Si on recule, on inverse la direction du volant pour que ça reste naturel
            if (forwardImpulse < 0) {
                turnSpeed = -turnSpeed;
            }
            this.setYRot(this.getYRot() + turnSpeed);
        }

        // 3. Application de la force (Inertie)
        Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());

        // Au lieu de dire "La vitesse EST X", on dit "On AJOUTE X à la vitesse actuelle"
        double addedMotionX = forwardVec.x * forwardImpulse * enginePower;
        double addedMotionZ = forwardVec.z * forwardImpulse * enginePower;

        this.setDeltaMovement(currentMotion.add(addedMotionX, 0, addedMotionZ));
    }

    protected void updateSpeed() {
        Vec3 currentMotion = this.getDeltaMovement();

        // 1. La Gravité
        double motionY = currentMotion.y;
        if (!this.isNoGravity()) {
            motionY -= 0.04;
        }

        // 2. La Friction de base (ralentissement)
        float friction = this.onGround() ? 0.9f : 0.98f;

        // --- NOUVEAU : ADHÉRENCE LATÉRALE (ANTI-DRIFT) ---
        if (this.onGround()) {
            // Dans quelle direction regarde exactement le kart ?
            Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());

            // Le "Dot Product" nous dit quelle fraction de notre inertie actuelle va
            // VRAIMENT dans la direction de notre capot (positif = on avance, négatif = on recule).
            double forwardSpeed = currentMotion.x * forwardVec.x + currentMotion.z * forwardVec.z;

            // On crée un mouvement "Idéal" (toute l'énergie est redirigée dans l'axe des roues)
            double idealX = forwardVec.x * forwardSpeed;
            double idealZ = forwardVec.z * forwardSpeed;

            // GRIP (Adhérence) : 1.0 = Un train sur des rails (0 drift). 0.0 = Patin à glace.
            // 0.85 est un bon réglage pour un kart (ça grippe fort, mais on sent un tout petit dérapage à haute vitesse)
            double grip = 0.85;

            // On mixe le mouvement actuel (glissant) avec le mouvement idéal (droit)
            double newMotionX = currentMotion.x + (idealX - currentMotion.x) * grip;
            double newMotionZ = currentMotion.z + (idealZ - currentMotion.z) * grip;

            // On applique la friction pour ralentir normalement
            newMotionX *= friction;
            newMotionZ *= friction;

            // On limite la chute
            motionY = motionY * 0.98;

            this.setDeltaMovement(newMotionX, motionY, newMotionZ);
        } else {
            // Si on est en l'air (en plein saut), on garde notre drift, c'est normal !
            this.setDeltaMovement(currentMotion.x * friction, motionY * 0.98, currentMotion.z * friction);
        }
        // -------------------------------------------------

        // 3. Application finale du mouvement
        this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
    }



    //Méthode pour générer les hitboxes
    private void spawnHitboxes() {
        // On boucle sur tous les slots enregistrés et on génère leur hitbox automatiquement !
        for (PartSlot slot : this.partSlots.values()) {
            createHitbox(slot.getId(), slot.getOffset(), slot.getHitboxWidth(), slot.getHitboxHeight());
        }
    }

    //Méthode pour créer une hitbox
    private void createHitbox(String slotId, Vec3 offset, float width, float height) {
        InteractionPartEntity hitbox = ModEntities.INTERACTION_PART.get().create(this.level());
        if (hitbox != null) {
            hitbox.init(this, slotId, offset, width, height);
            hitbox.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(hitbox);
            this.hitboxes.add(hitbox);
        }
    }



    //Déclaration des variables synchronisées entre le client et le serveur
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

    //Mise à jour des variables dans l'entité
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
        if (entity instanceof InteractionPartEntity) {
            return false;
        }
        return super.canCollideWith(entity);
    }


    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        // Ne PAS appeler super.lerpTo() — c'est lui qui causait le rollback
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
            callback.accept(passenger, this.getX(), this.getY() + 0.2, this.getZ());
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

    @Override
    public float maxUpStep() {
        return 1.0f;
    }



}
