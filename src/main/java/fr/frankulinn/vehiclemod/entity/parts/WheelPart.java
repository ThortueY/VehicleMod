package fr.frankulinn.vehiclemod.entity.parts;

public class WheelPart extends VehiclePart {
    private final float grip;
    private final String id;


    public WheelPart(float grip, float weight, String id) {
        super(weight, id);
        this.grip = grip;
        this.id = id;
    }

    public float getGrip() {
        return grip;
    }

    public String getId() {
        return id;
    }
}
