package fr.frankulinn.vehiclemod.entity.parts;

public class WheelPart extends VehiclePart {
    private final float grip; // L'adhérence (utile plus tard pour la glace/boue)

    public WheelPart(float grip, float weight) {
        super(weight);
        this.grip = grip;
    }

    public float getGrip() {
        return this.grip;
    }
}
