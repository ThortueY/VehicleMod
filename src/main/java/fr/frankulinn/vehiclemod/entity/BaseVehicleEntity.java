package fr.frankulinn.vehiclemod.entity;

import fr.frankulinn.vehiclemod.entity.parts.InteractionPartEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.registers.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;

import javax.annotation.Nullable;
import java.util.*;

public abstract class BaseVehicleEntity extends Entity implements GeoEntity {

    public static final EntityDataAccessor<CompoundTag> PARTS_SYNC = SynchedEntityData.defineId(BaseVehicleEntity.class,
            EntityDataSerializers.COMPOUND_TAG);

    // Variables pour le réservoir
    public static final EntityDataAccessor<Float> FUEL_LEVEL = SynchedEntityData.defineId(BaseVehicleEntity.class,
            EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VEHICLE_PITCH = SynchedEntityData.defineId(BaseVehicleEntity.class,
            EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Float> VEHICLE_ROLL = SynchedEntityData.defineId(BaseVehicleEntity.class,
            EntityDataSerializers.FLOAT);

    public abstract float getMaxFuel(); // La capacité maximum du réservoir

    // Slots pour les pièces de véhicule
    private final Map<String, PartSlot> partSlots = new HashMap<>();
    private final List<InteractionPartEntity> hitboxes = new ArrayList<>();
    private boolean hitboxesSpawned = false;

    // Variables pour la physique du véhicule
    public float wheelRotation = 0.0f;
    public float prevWheelRotation = 0.0f;
    public float steeringAngle = 0.0f;
    public float prevSteeringAngle = 0.0f;
    public float prevVehiclePitch = 0.0f;
    public float prevVehicleRoll = 0.0f;

    @Nullable
    public Player refuelingPlayer;
    public InteractionHand refuelingHand;
    public int refuelingTimeout = 0; // Ticks restants avant l'arrêt du remplissage si pas de nouveau clic

    private final java.util.Map<java.util.UUID, String> passengerSeats = new java.util.HashMap<>();

    // Constructeur obligatoire pour que Minecraft puisse spawner l'entité
    public BaseVehicleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        initSlots();
    }

    // Constructeur permettant de créer les slots
    protected abstract void initSlots();

    // Méthode pour ajouter un slot
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

        // 3.5 Calcul de l'inclinaison (pitch) sur les pentes
        this.updatePitch();

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
                // Le "4.0f" dépend de la taille de ta roue. Si elle tourne trop lentement,
                // augmente-le !
                float rotationSpeed = (float) speed * 4.0f;
                this.wheelRotation += isReversing ? -rotationSpeed : rotationSpeed;
            }

            // Calcul du braquage (volant)
            if (this.getControllingPassenger() instanceof Player driver) {
                // xxa représente les touches Q/D (ou A/D). On multiplie par 35 degrés max.
                float targetSteering = -driver.xxa * 35.0f;

                // On lisse le mouvement avec un Lerp pour que les roues ne tournent pas de
                // façon saccadée
                this.steeringAngle += (targetSteering - this.steeringAngle) * 0.2f;
            } else {
                // Si personne ne conduit, les roues se remettent droites doucement
                this.steeringAngle += (0.0f - this.steeringAngle) * 0.1f;
            }
        }

        // --- NOUVEAU : 5. Transfert continu de carburant ---
        if (this.refuelingPlayer != null) {
            this.refuelingTimeout--;

            if (this.refuelingTimeout <= 0
                    || !(this.refuelingPlayer.getItemInHand(this.refuelingHand)
                            .getItem() instanceof fr.frankulinn.vehiclemod.item.JerricanItem)
                    || this.distanceTo(this.refuelingPlayer) > 5.0) {
                // Le joueur a relâché le clic (timeout) ou s'est éloigné
                this.refuelingPlayer = null;
            } else {
                // Transfert d'essence tous les 4 ticks
                if (this.tickCount % 4 == 0) {
                    net.minecraft.world.item.ItemStack jerricanStack = this.refuelingPlayer
                            .getItemInHand(this.refuelingHand);
                    float vehicleFuel = this.entityData.get(FUEL_LEVEL);
                    float jerricanFuel = fr.frankulinn.vehiclemod.item.JerricanItem.getFuel(jerricanStack);
                    float spaceLeft = this.getMaxFuel() - vehicleFuel;

                    float toTransfer = Math.min(2.0f, Math.min(spaceLeft, jerricanFuel));

                    if (toTransfer > 0) {
                        if (!this.level().isClientSide()) {
                            this.entityData.set(FUEL_LEVEL, vehicleFuel + toTransfer);
                            if (!this.refuelingPlayer.isCreative()) {
                                fr.frankulinn.vehiclemod.item.JerricanItem.setFuel(jerricanStack,
                                        jerricanFuel - toTransfer);
                                // Force l'envoi de la mise à jour de l'item au client
                                if (this.refuelingPlayer instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                                    serverPlayer.inventoryMenu.broadcastChanges();
                                }
                            }
                            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                                    net.minecraft.sounds.SoundEvents.BUCKET_EMPTY,
                                    net.minecraft.sounds.SoundSource.PLAYERS,
                                    0.2f, 0.8f + this.level().random.nextFloat() * 0.4f);
                        }
                    } else if (spaceLeft <= 0) {
                        this.refuelingPlayer = null;
                    } else if (jerricanFuel <= 0) {
                        this.refuelingPlayer = null;
                    }
                }
            }
        }
    }

    public void startRefueling(Player player, InteractionHand hand) {
        this.refuelingPlayer = player;
        this.refuelingHand = hand;
        this.refuelingTimeout = 5; // Le joueur a 5 ticks pour recliquer (maintenir), sinon ça s'arrête
    }

    protected void updateAcceleration(Player driver) {
        float forwardImpulse = driver.zza; // Z = Avancer/Reculer
        float strafeImpulse = -driver.xxa; // X = Gauche/Droite

        double enginePower = 0.0;
        float currentFuel = this.entityData.get(FUEL_LEVEL);

        boolean isEngineSecured = false;
        int securedWheels = 0;

        // --- CORRECTION CLIENT/SERVEUR ---
        if (this.level().isClientSide()) {
            // Le client regarde le dictionnaire visuel ET l'état "vissé"
            CompoundTag syncedParts = this.entityData.get(PARTS_SYNC);

            String engine = syncedParts.getString("engine_bay");
            boolean engineSecured = syncedParts.getBoolean("engine_bay_secured"); // On lit le booléen
            isEngineSecured = (engine != null && !engine.isEmpty() && !engine.equals("none") && engineSecured);

            for (PartSlot slot : this.getPartSlots()) {
                String wheelId = syncedParts.getString(slot.getId());
                boolean wheelSecured = syncedParts.getBoolean(slot.getId() + "_secured"); // On lit le booléen

                if (slot.getId().startsWith("wheel") && wheelId != null && !wheelId.isEmpty() && !wheelId.equals("none")
                        && wheelSecured) {
                    securedWheels++;
                }
            }
        } else {
            // Le serveur regarde les vrais objets (Aucun changement ici)
            PartSlot engineSlot = this.getSlot("engine_bay");
            isEngineSecured = engineSlot != null && engineSlot.getPart() != null && engineSlot.isSecured();

            for (PartSlot slot : this.getPartSlots()) {
                if (slot.getId().startsWith("wheel") && slot.isSecured()) {
                    securedWheels++;
                }
            }
        }
        // 1. Calcul de la puissance du moteur
        if (isEngineSecured && currentFuel > 0) {
            enginePower = 0.05;

            // La consommation d'essence se fait UNIQUEMENT sur le serveur
            if (forwardImpulse != 0 && !this.level().isClientSide()) {
                float consumptionRate = 0.05f;
                PartSlot engineSlot = this.getSlot("engine_bay");
                if (engineSlot != null && engineSlot
                        .getPart() instanceof fr.frankulinn.vehiclemod.entity.parts.EnginePart enginePart) {
                    consumptionRate = enginePart.getFuelConsumption();
                }

                float newFuel = Math.max(0.0f, currentFuel - consumptionRate);
                this.entityData.set(FUEL_LEVEL, newFuel);
            }
        }

        enginePower *= (securedWheels / 4.0);

        // 2. On tourne UNIQUEMENT si le kart est en train de rouler (Vitesse > 0.05)
        Vec3 currentMotion = this.getDeltaMovement();
        double horizontalSpeed = currentMotion.horizontalDistance();

        if (horizontalSpeed > 0.05) {
            // Plus on va vite, plus le volant est réactif (tu pourras ajuster ce
            // multiplicateur)
            float turnSpeed = strafeImpulse * 4.0f;

            // Si on recule, on inverse la direction du volant pour que ça reste naturel
            if (forwardImpulse < 0) {
                turnSpeed = -turnSpeed;
            }
            this.setYRot(this.getYRot() + turnSpeed);
        }

        // 3. Application de la force (Inertie)
        Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());

        // Au lieu de dire "La vitesse EST X", on dit "On AJOUTE X à la vitesse
        // actuelle"
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
            Vec3 forwardVec = Vec3.directionFromRotation(0, this.getYRot());
            double forwardSpeed = currentMotion.x * forwardVec.x + currentMotion.z * forwardVec.z;

            // CORRECTION INSTABILITÉ : On garde la Vitesse brute (Magnitude)
            double currentMag = currentMotion.horizontalDistance();
            double sign = (forwardSpeed >= 0) ? 1.0 : -1.0;

            // Le vecteur idéal conserve 100% de la puissance dans la nouvelle direction
            double idealX = forwardVec.x * currentMag * sign;
            double idealZ = forwardVec.z * currentMag * sign;

            double grip = 0.85;

            double newMotionX = currentMotion.x + (idealX - currentMotion.x) * grip;
            double newMotionZ = currentMotion.z + (idealZ - currentMotion.z) * grip;

            newMotionX *= friction;
            newMotionZ *= friction;
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

    // --- INCLINAISON DU VÉHICULE SUR LES PENTES (PITCH + ROLL) ---
    private void updatePitch() {
        if (this.level().isClientSide()) {
            // Côté client, on sauvegarde les valeurs précédentes pour l'interpolation
            this.prevVehiclePitch = this.entityData.get(VEHICLE_PITCH);
            this.prevVehicleRoll = this.entityData.get(VEHICLE_ROLL);
            return;
        }

        float yawRad = this.getYRot() * ((float) Math.PI / 180F);

        // === PITCH (avant/arrière) ===
        float halfLength = 1.2f;
        double frontX = this.getX() + (-Math.sin(yawRad) * halfLength);
        double frontZ = this.getZ() + (Math.cos(yawRad) * halfLength);
        double backX = this.getX() + (Math.sin(yawRad) * halfLength);
        double backZ = this.getZ() + (-Math.cos(yawRad) * halfLength);

        double frontGroundY = getGroundHeight(frontX, this.getY(), frontZ);
        double backGroundY = getGroundHeight(backX, this.getY(), backZ);

        float targetPitch = (float) Math.toDegrees(Math.atan2(frontGroundY - backGroundY, halfLength * 2.0));
        targetPitch = net.minecraft.util.Mth.clamp(targetPitch, -30.0f, 30.0f);

        float currentPitch = this.entityData.get(VEHICLE_PITCH);
        this.entityData.set(VEHICLE_PITCH, currentPitch + (targetPitch - currentPitch) * 0.3f);

        // === ROLL (gauche/droite) ===
        float halfWidth = 0.8f;
        // Direction perpendiculaire (90° à droite du véhicule)
        double rightX = this.getX() + (Math.cos(yawRad) * halfWidth);
        double rightZ = this.getZ() + (Math.sin(yawRad) * halfWidth);
        double leftX = this.getX() + (-Math.cos(yawRad) * halfWidth);
        double leftZ = this.getZ() + (-Math.sin(yawRad) * halfWidth);

        double rightGroundY = getGroundHeight(rightX, this.getY(), rightZ);
        double leftGroundY = getGroundHeight(leftX, this.getY(), leftZ);

        float targetRoll = (float) Math.toDegrees(Math.atan2(rightGroundY - leftGroundY, halfWidth * 2.0));
        targetRoll = net.minecraft.util.Mth.clamp(targetRoll, -20.0f, 20.0f);

        float currentRoll = this.entityData.get(VEHICLE_ROLL);
        this.entityData.set(VEHICLE_ROLL, currentRoll + (targetRoll - currentRoll) * 0.3f);
    }

    public float getVehiclePitch() {
        return this.entityData.get(VEHICLE_PITCH);
    }

    public float getVehicleRoll() {
        return this.entityData.get(VEHICLE_ROLL);
    }

    private double getGroundHeight(double x, double entityY, double z) {
        BlockPos pos = BlockPos.containing(x, entityY + 1, z);
        // Chercher le sol en dessous (max 4 blocs)
        for (int i = 0; i < 4; i++) {
            BlockPos checkPos = pos.below(i);
            BlockState state = this.level().getBlockState(checkPos);
            VoxelShape collisionShape = state.getCollisionShape(this.level(), checkPos);
            // Ignorer les blocs sans collision (fleurs, herbe, etc.)
            if (!collisionShape.isEmpty()) {
                // Utiliser la hauteur réelle de la collision shape (dalles = 0.5, neige =
                // variable, bloc plein = 1.0)
                return checkPos.getY() + collisionShape.max(net.minecraft.core.Direction.Axis.Y);
            }
        }
        // Si pas de sol trouvé, retourner la position actuelle
        return entityY;
    }

    // Méthode pour générer les hitboxes
    private void spawnHitboxes() {
        // On boucle sur tous les slots enregistrés et on génère leur hitbox
        // automatiquement !
        for (PartSlot slot : this.partSlots.values()) {
            createHitbox(slot.getId(), slot.getOffset(), slot.getHitboxWidth(), slot.getHitboxHeight());
        }
    }

    // Méthode pour créer une hitbox
    private void createHitbox(String slotId, Vec3 offset, float width, float height) {
        InteractionPartEntity hitbox = ModEntities.INTERACTION_PART.get().create(this.level());
        if (hitbox != null) {
            hitbox.init(this, slotId, offset, width, height);
            hitbox.setPos(this.getX(), this.getY(), this.getZ());
            this.level().addFreshEntity(hitbox);
            this.hitboxes.add(hitbox);
        }
    }

    // Déclaration des variables synchronisées entre le client et le serveur
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(PARTS_SYNC, new CompoundTag());
        builder.define(FUEL_LEVEL, 0.0f);
        builder.define(VEHICLE_PITCH, 0.0f);
        builder.define(VEHICLE_ROLL, 0.0f);
    }

    // Mise à jour des variables dans l'entité
    public void updatePartsSync() {
        if (this.level().isClientSide())
            return;

        CompoundTag syncTag = new CompoundTag();

        for (fr.frankulinn.vehiclemod.entity.parts.PartSlot slot : this.getPartSlots()) {
            if (slot.getPart() != null) {
                // On garde l'ID de la pièce pour l'affichage visuel
                syncTag.putString(slot.getId(), slot.getPart().getId());

                // --- NOUVEAU : On informe le client si la pièce est vissée ou non ! ---
                syncTag.putBoolean(slot.getId() + "_secured", slot.isSecured());
            }
        }

        this.entityData.set(PARTS_SYNC, syncTag);
    }

    private String getWheelTypeAt(String slotId) {
        PartSlot slot = this.getSlot(slotId);
        if (slot != null && slot.getPart() instanceof fr.frankulinn.vehiclemod.entity.parts.WheelPart wheel) {
            return wheel.getId();
        }
        return "none";
    }

    private boolean isWheelSecured(String slotId) {
        PartSlot slot = this.getSlot(slotId);
        return slot != null && slot.isSecured()
                && slot.getPart() instanceof fr.frankulinn.vehiclemod.entity.parts.WheelPart;
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
        // NO-OP : Le client et le serveur calculent tous les deux la physique.
        // La position initiale vient du spawn packet, pas de lerpTo().
        // On ignore TOUS les sync packets pour éviter le rollback au démontage,
        // car il y a une race condition : isVehicle() passe à false côté client
        // AVANT que le dernier paquet de position du serveur n'arrive.
    }

    // Sauvegarde des données quand on quitte le monde
    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        CompoundTag slotsTag = new CompoundTag();

        // On sauvegarde chaque emplacement par son nom ("engine_bay", etc.)
        for (java.util.Map.Entry<String, fr.frankulinn.vehiclemod.entity.parts.PartSlot> entry : this.partSlots
                .entrySet()) {
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
        // On ne monte que si un siège fixé est libre
        if (!this.level().isClientSide() && player.getVehicle() == null) {
            for (PartSlot slot : this.getPartSlots()) {
                if (slot.getId().startsWith("seat") && slot.isSecured() && !isSeatOccupied(slot.getId())) {
                    assignSeat(player, slot.getId());
                    player.startRiding(this);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.PASS; // Pas de siège libre
        }
        return super.interact(player, hand);
    }

    public void assignSeat(Entity passenger, String slotId) {
        this.passengerSeats.put(passenger.getUUID(), slotId);
    }

    // Vérifier si un siège est déjà pris par quelqu'un dans la voiture
    public boolean isSeatOccupied(String slotId) {
        for (Entity p : this.getPassengers()) {
            if (slotId.equals(this.passengerSeats.get(p.getUUID()))) {
                return true;
            }
        }
        return false;
    }

    // Nettoyer le registre quand un joueur descend de la voiture
    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.passengerSeats.remove(passenger.getUUID());
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction callback) {
        PartSlot seatSlot = null;

        if (this.level().isClientSide()) {
            // Côté CLIENT : passengerSeats n'est pas synchronisé et PartSlot.isSecured()
            // est toujours false.
            // On cherche le premier slot "seat_" qui est marqué secured dans le CompoundTag
            // synchronisé.
            CompoundTag syncedParts = this.entityData.get(PARTS_SYNC);
            for (PartSlot slot : this.getPartSlots()) {
                if (slot.getId().startsWith("seat") && syncedParts.getBoolean(slot.getId() + "_secured")) {
                    seatSlot = slot;
                    break;
                }
            }
        } else {
            // Côté SERVEUR : on utilise le vrai système d'assignation
            String targetSlotId = this.passengerSeats.get(passenger.getUUID());

            if (targetSlotId == null) {
                for (PartSlot slot : this.getPartSlots()) {
                    if (slot.getId().startsWith("seat") && slot.isSecured() && !isSeatOccupied(slot.getId())) {
                        targetSlotId = slot.getId();
                        assignSeat(passenger, targetSlotId);
                        break;
                    }
                }
            }

            seatSlot = targetSlotId != null ? this.getSlot(targetSlotId) : null;
            // Vérification serveur : le slot doit être secured
            if (seatSlot != null && !seatSlot.isSecured()) {
                seatSlot = null;
            }
        }

        // Positionner le joueur aux coordonnées 3D du siège
        if (seatSlot != null) {
            Vec3 offset = seatSlot.getOffset();
            // Même rotation que InteractionPartEntity pour un positionnement correct
            Vec3 rotatedOffset = offset.yRot(-this.getYRot() * ((float) Math.PI / 180F));

            // Le centre vertical de la hitbox du siège
            double seatCenterY = offset.y + seatSlot.getHitboxHeight() / 2.0;
            // Le centre vertical de la hitbox du joueur (hauteur / 2)
            double passengerCenterOffset = passenger.getBbHeight() / 2.0;

            callback.accept(passenger, this.getX() + rotatedOffset.x, this.getY() + seatCenterY - passengerCenterOffset,
                    this.getZ() + rotatedOffset.z);
        } else {
            super.positionRider(passenger, callback);
        }
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        // Vérifier si le siège conducteur est fixé
        boolean isSeatSecured;
        if (this.level().isClientSide()) {
            // Côté CLIENT : les PartSlot ne sont jamais mis à jour, on lit le CompoundTag
            // synchronisé
            CompoundTag syncedParts = this.entityData.get(PARTS_SYNC);
            isSeatSecured = syncedParts.getBoolean("seat_driver_secured");
        } else {
            // Côté SERVEUR : on lit le vrai objet PartSlot
            PartSlot driverSlot = this.getSlot("seat_driver");
            isSeatSecured = driverSlot != null && driverSlot.isSecured();
        }

        if (!isSeatSecured) {
            return null;
        }

        if (this.level().isClientSide()) {
            // Côté CLIENT : passengerSeats n'est pas synchronisé, on prend le premier
            // passager
            Entity first = this.getFirstPassenger();
            return first instanceof LivingEntity living ? living : null;
        }

        // Côté SERVEUR : on cherche le passager assigné au "seat_driver"
        for (Entity passenger : this.getPassengers()) {
            if ("seat_driver".equals(this.passengerSeats.get(passenger.getUUID()))) {
                return passenger instanceof LivingEntity living ? living : null;
            }
        }
        return null;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        int availableSeats = 0;

        // On compte chaque emplacement qui commence par "seat" et qui est vissé
        for (fr.frankulinn.vehiclemod.entity.parts.PartSlot slot : this.getPartSlots()) {
            if (slot.getId().startsWith("seat") && slot.isSecured()) {
                availableSeats++;
            }
        }

        // Autorisé seulement s'il reste une place assise
        return this.getPassengers().size() < availableSeats;
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
    protected void playStepSound(net.minecraft.core.BlockPos pos,
            net.minecraft.world.level.block.state.BlockState blockIn) {
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
