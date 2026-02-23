package fr.frankulinn.vehiclemod.entity.parts;

public class SeatPart extends VehiclePart {
    private final String modelId;

    public SeatPart(float weight, String modelId) {
        super(weight, modelId); // Appelle le constructeur de VehiclePart (pour gérer le poids)
        this.modelId = modelId;
    }

    public String getModelId() {
        return this.modelId;
    }
}
