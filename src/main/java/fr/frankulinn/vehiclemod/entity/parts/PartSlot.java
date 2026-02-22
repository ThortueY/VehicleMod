package fr.frankulinn.vehiclemod.entity.parts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public class PartSlot {

    private final String slotId; // ex: "engine_bay", "wheel_front_left"
    private PartState state;
    private VehiclePart installedPart;
    private final Vec3 offset;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final SlotInteraction interactionBehavior;

    public PartSlot(String id, Vec3 offset, float hitboxWidth, float hitboxHeight, SlotInteraction interactionBehavior) {
        this.slotId = id;
        this.offset = offset;
        this.state = fr.frankulinn.vehiclemod.entity.parts.PartState.EMPTY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.interactionBehavior = interactionBehavior;
    }

    public net.minecraft.world.phys.Vec3 getOffset() {
        return this.offset;
    }

    // Le joueur pose la pièce à la main
    public boolean installPart(VehiclePart part) {
        if (this.state == PartState.EMPTY) {
            this.installedPart = part;
            this.state = PartState.PLACED;
            return true;
        }
        return false; // Déjà occupé
    }

    // Le joueur utilise sa clé BTR ou sa visseuse
    public boolean securePart() {
        if (this.state == PartState.PLACED) {
            this.state = PartState.SECURED;
            return true;
        }
        return false;
    }

    // Le joueur démonte la pièce
    public VehiclePart removePart() {
        VehiclePart part = this.installedPart;
        this.installedPart = null;
        this.state = PartState.EMPTY;
        return part; // Retourne la pièce pour la redonner au joueur
    }

    public boolean unsecurePart() {
        if (this.state == fr.frankulinn.vehiclemod.entity.parts.PartState.SECURED) {
            this.state = fr.frankulinn.vehiclemod.entity.parts.PartState.PLACED;
            return true;
        }
        return false;
    }

    public float getHitboxWidth() { return this.hitboxWidth; }
    public float getHitboxHeight() { return this.hitboxHeight; }

    public boolean isSecured() {
        return this.state == PartState.SECURED;
    }

    public boolean isEmpty() {
        return this.state == PartState.EMPTY;
    }

    public VehiclePart getPart() {
        return this.installedPart;
    }

    public String getId() {
        return this.slotId;
    }

    public SlotInteraction getInteractionBehavior() {
        return interactionBehavior;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("State", this.state.name()); // Sauvegarde EMPTY, PLACED ou SECURED

        if (this.installedPart != null) {
            CompoundTag partTag = new CompoundTag();

            // On sauvegarde les spécificités selon le type de pièce
            if (this.installedPart instanceof fr.frankulinn.vehiclemod.entity.parts.EnginePart engine) {
                partTag.putString("Type", "Engine");
                partTag.putFloat("Horsepower", engine.getHorsepower());
                partTag.putFloat("Weight", engine.getWeight());

                // --- NOUVEAU : ON SAUVEGARDE ENFIN L'ESSENCE ET LA VITESSE ! ---
                partTag.putFloat("MaxSpeed", engine.getMaxSpeed());
                partTag.putFloat("FuelConsumption", engine.getFuelConsumption());
            }
            else if (this.installedPart instanceof fr.frankulinn.vehiclemod.entity.parts.WheelPart wheel) {
                partTag.putString("Type", "Wheel");
                partTag.putFloat("Grip", wheel.getGrip());
                partTag.putFloat("Weight", wheel.getWeight());
                partTag.putString("WheelType", wheel.getWheelType()); // "offroad", "kart", etc.
            }

            // Stats communes
            partTag.putFloat("Condition", this.installedPart.getCondition());
            tag.put("Part", partTag);
        }
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("State")) {
            // Attention à bien mettre le bon chemin vers ton enum PartState !
            this.state = fr.frankulinn.vehiclemod.entity.parts.PartState.valueOf(tag.getString("State"));
        }

        if (tag.contains("Part")) {
            CompoundTag partTag = tag.getCompound("Part");
            String type = partTag.getString("Type");
            float weight = partTag.getFloat("Weight");
            float condition = partTag.getFloat("Condition");

            // On recrée l'objet pièce selon son type sauvegardé
            if (type.equals("Engine")) {
                float hp = partTag.getFloat("Horsepower");
                float maxSpeed = partTag.getFloat("MaxSpeed");
                float fuelConsumption = partTag.getFloat("FuelConsumption");
                this.installedPart = new fr.frankulinn.vehiclemod.entity.parts.EnginePart(hp, weight,maxSpeed, fuelConsumption );
            }
            else if (type.equals("Wheel")) {
                float grip = partTag.getFloat("Grip");
                String wheelType = partTag.getString("WheelType");
                this.installedPart = new fr.frankulinn.vehiclemod.entity.parts.WheelPart(grip, weight, wheelType);
            }

            if (this.installedPart != null) {
                this.installedPart.setCondition(condition);
            }
        } else {
            this.installedPart = null;
            this.state = fr.frankulinn.vehiclemod.entity.parts.PartState.EMPTY;
        }
    }
}
