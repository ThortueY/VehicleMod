package fr.frankulinn.vehiclemod.entity.parts;

public class WheelPart extends VehiclePart {
    private final float grip;
    private final String wheelType; // L'identifiant du modèle (ex: "offroad", "kart")

    public WheelPart(float grip, float weight, String wheelType) {
        super(weight);
        this.grip = grip;
        this.wheelType = wheelType;
    }

    public String getWheelType() {
        return this.wheelType;
    }

    public float getGrip() {
        return grip;
    }
}
