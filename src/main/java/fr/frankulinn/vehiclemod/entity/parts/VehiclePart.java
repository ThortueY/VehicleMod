package fr.frankulinn.vehiclemod.entity.parts;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;

public abstract class VehiclePart {

    private final float baseWeight;
    private float condition; // De 0.0f (détruit) à 1.0f (neuf)

    public VehiclePart(float baseWeight) {
        this.baseWeight = baseWeight;
        this.condition = 1.0f; // Neuf par défaut
    }

    // Cette méthode sera appelée à chaque tick du véhicule (ex: pour consommer de l'essence ou user le pneu)
    public void tickPart(BaseVehicleEntity vehicle) {
        // Logique par défaut vide
    }

    public float getWeight() {
        return this.baseWeight;
    }

    public float getCondition() {
        return this.condition;
    }

    public void setCondition(float condition) {
        this.condition = Math.max(0.0f, Math.min(1.0f, condition)); // Garde entre 0 et 1
    }
}
