package fr.frankulinn.vehiclemod.entity.parts;

public class EnginePart extends VehiclePart {

    private final float horsepower;

    public EnginePart(float horsepower, float weight) {
        super(weight); // Le poids du moteur
        this.horsepower = horsepower;
    }

    public float getHorsepower() {
        return this.horsepower;
    }
}
