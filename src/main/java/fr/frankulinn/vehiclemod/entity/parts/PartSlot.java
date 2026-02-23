package fr.frankulinn.vehiclemod.entity.parts;

import fr.frankulinn.vehiclemod.item.EngineItem;
import fr.frankulinn.vehiclemod.item.SeatItem;
import fr.frankulinn.vehiclemod.item.WheelItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class PartSlot {

    private final String slotId; // ex: "engine_bay", "wheel_front_left"
    private PartState state;
    private VehiclePart installedPart;
    private final Vec3 offset;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final SlotInteraction interactionBehavior;
    private final PartCategory allowedCategory;

    public PartSlot(String id, Vec3 offset, float hitboxWidth, float hitboxHeight, SlotInteraction interactionBehavior,
            PartCategory allowedCategory) {
        this.slotId = id;
        this.offset = offset;
        this.state = PartState.EMPTY;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.interactionBehavior = interactionBehavior;
        this.allowedCategory = allowedCategory;
    }

    public PartCategory getAllowedCategory() {
        return this.allowedCategory;
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

    public float getHitboxWidth() {
        return this.hitboxWidth;
    }

    public float getHitboxHeight() {
        return this.hitboxHeight;
    }

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

            partTag.putString("id", installedPart.getId()); // Sauvegarde l'ID de l'item
            partTag.putFloat("Condition", this.installedPart.getCondition()); // Sauvegarde son état
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
            String id = partTag.getString("id");
            Item registredPartItem = BuiltInRegistries.ITEM
                    .get(ResourceLocation.fromNamespaceAndPath("vehiclemod", id));

            if (registredPartItem instanceof EngineItem engineItem) {
                this.installedPart = engineItem.createPart();
            }

            if (registredPartItem instanceof WheelItem wheelItem) {
                this.installedPart = wheelItem.createPart();
            }
            if (registredPartItem instanceof SeatItem seatItem) {
                this.installedPart = seatItem.createPart();
            }
            if (this.installedPart != null) {
                this.installedPart.setCondition(condition);
            }

        } else {
            this.installedPart = null;
            this.state = PartState.EMPTY;
        }

    }

}
