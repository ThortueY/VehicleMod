package fr.frankulinn.vehiclemod.entity.parts;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import fr.frankulinn.vehiclemod.item.EngineItem;
import fr.frankulinn.vehiclemod.item.WheelItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
        double halfW = width / 2.0;
        double halfH = height / 2.0;
        this.setBoundingBox(new AABB(-halfW, -halfH, -halfW, halfW, halfH, halfW));
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
        // 1. LE CLIENT : Il s'arrête ici mais renvoie SUCCESS pour forcer l'envoi du
        // paquet au Serveur !
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        // 2. LE SERVEUR : Gère toute la logique à partir d'ici
        if (this.parentVehicle == null)
            return InteractionResult.PASS;

        fr.frankulinn.vehiclemod.entity.parts.PartSlot slot = this.parentVehicle.getSlot(this.slotId);
        if (slot == null)
            return InteractionResult.PASS;

        net.minecraft.world.item.ItemStack stackInHand = player.getItemInHand(hand);

        // SI L'EMPLACEMENT EST VIDE
        if (slot.isEmpty()) {
            if (stackInHand.isEmpty()) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§eEmplacement : " + this.slotId + " (Vide)"),
                        true);
                return InteractionResult.SUCCESS;
            }

            if (this.slotId.equals("fuel_cap")) {
                if (stackInHand.getItem() instanceof fr.frankulinn.vehiclemod.item.JerricanItem) {
                    float currentFuel = this.parentVehicle.getEntityData().get(VehicleEntity.FUEL_LEVEL);

                    if (currentFuel < VehicleEntity.MAX_FUEL) {
                        this.parentVehicle.getEntityData().set(VehicleEntity.FUEL_LEVEL, VehicleEntity.MAX_FUEL);

                        if (!player.isCreative()) {
                            stackInHand.shrink(1);
                        }
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aPlein effectué ! ⛽ (100%)"), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eLe réservoir est déjà plein !"), true);
                    }
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cC'est la trappe à essence. Utilisez un Jerrican ici !"), true);
                }
                return InteractionResult.SUCCESS;
            }

            // LOGIQUE MOTEUR
            if (this.slotId.equals("engine_bay")) {
                if (stackInHand.getItem() instanceof EngineItem) {
                    fr.frankulinn.vehiclemod.entity.parts.EnginePart newEngine = new fr.frankulinn.vehiclemod.entity.parts.EnginePart(
                            150.0f, 200.0f);

                    if (slot.installPart(newEngine)) {
                        if (!player.isCreative())
                            stackInHand.shrink(1);
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§aMoteur posé ! (Non fixé)"), true);
                        this.parentVehicle.updatePartsSync(); // Met à jour le Client
                    }
                } else {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§cCet emplacement nécessite un Moteur !"),
                            true);
                }
                return InteractionResult.CONSUME;
            }
            // LOGIQUE ROUES
            else if (this.slotId.startsWith("wheel_")) {
                if (stackInHand.getItem() instanceof WheelItem) {
                    WheelPart newWheel = new WheelPart(1.0f, 15.0f, "kart_wheel");

                    if (slot.installPart(newWheel)) {
                        if (!player.isCreative())
                            stackInHand.shrink(1);
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§aRoue posée ! (Non fixée)"), true);
                        this.parentVehicle.updatePartsSync(); // Met à jour le Client
                    }
                } else {
                    player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("§cCet emplacement nécessite une Roue !"),
                            true);
                }
                return InteractionResult.CONSUME;
            }
        }
        // SI L'EMPLACEMENT EST OCCUPÉ
        else if (slot.getPart() != null) {
            if (!slot.isSecured()) {
                if (stackInHand.getItem() instanceof WrenchItem) {
                    if (slot.securePart()) {
                        player.displayClientMessage(
                                net.minecraft.network.chat.Component.literal("§aPièce fixée et prête à l'usage !"),
                                true);
                        this.parentVehicle.updatePartsSync();
                    }
                } else if (stackInHand.isEmpty()) {
                    slot.removePart();
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§ePièce retirée !"),
                            true);
                    this.parentVehicle.updatePartsSync();
                } else {
                    player.displayClientMessage(net.minecraft.network.chat.Component
                            .literal("§cUtilisez une Clé pour fixer, ou les mains vides pour enlever."), true);
                }
            } else {
                if (stackInHand.getItem() instanceof WrenchItem) {
                    if (slot.unsecurePart()) {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§ePièce dévissée."), true);
                        this.parentVehicle.updatePartsSync();
                    }
                }
                // --- NOUVEAU : LE PLEIN D'ESSENCE ---
                else if (stackInHand.getItem() instanceof fr.frankulinn.vehiclemod.item.JerricanItem && this.slotId.equals("engine_bay")) {
                    float currentFuel = this.parentVehicle.getEntityData().get(VehicleEntity.FUEL_LEVEL);

                    if (currentFuel < VehicleEntity.MAX_FUEL) {
                        // On remplit au max !
                        this.parentVehicle.getEntityData().set(VehicleEntity.FUEL_LEVEL, VehicleEntity.MAX_FUEL);

                        // On consomme le jerrican en survie
                        if (!player.isCreative()) {
                            stackInHand.shrink(1);
                        }

                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aPlein effectué ! ⛽ (100%)"), true);
                    } else {
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eLe réservoir est déjà plein !"), true);
                    }
                }
                else {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cCette pièce est vissée ! Utilisez une Clé (ou un Jerrican pour le moteur)."), true);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    // Petite méthode utilitaire pour traduire les ID techniques en joli texte pour
    // les joueurs
    private String getReadableSlotName(String slotId) {
        return switch (slotId) {
            case "engine_bay" -> "Baie Moteur";
            case "wheel_front_left" -> "Roue Avant Gauche";
            case "wheel_front_right" -> "Roue Avant Droite";
            case "wheel_back_left" -> "Roue Arrière Gauche";
            case "wheel_back_right" -> "Roue Arrière Droite";
            case "fuel_cap" -> "Trappe à Essence";
            default -> "Pièce Inconnue";
        };
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
