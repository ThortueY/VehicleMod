package fr.frankulinn.vehiclemod.entity.parts;

public class EnginePart extends VehiclePart {

    private final float horsepower;
    private final float maxSpeed;
    private final float fuelConsumption;

    public EnginePart(float horsepower, float weight, float maxSpeed, float fuelConsumption) {
        super(weight); // Le poids du moteur
        this.horsepower = horsepower;
        this.maxSpeed = maxSpeed;
        this.fuelConsumption = fuelConsumption;
    }

    public float getHorsepower() {
        return this.horsepower;
    }

    public float getFuelConsumption() {
        return this.fuelConsumption;
    }

    public float getMaxSpeed() {
        return this.maxSpeed;
    }
}
