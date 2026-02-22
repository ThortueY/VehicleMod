package fr.frankulinn.vehiclemod.entity.parts;

public class EnginePart extends VehiclePart {
    private final float horsepower;
    private final float maxSpeed;
    private final float fuelConsumption;

    public EnginePart(float horsepower, float weight, float maxSpeed, float fuelConsumption) {
        super(weight);
        this.horsepower = horsepower;
        this.maxSpeed = maxSpeed;
        this.fuelConsumption = fuelConsumption;
    }

    // ... tes autres getters

    public float getFuelConsumption() {
        return fuelConsumption;
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public float getHorsepower() {
        return horsepower;
    }
}
