package fr.frankulinn.vehiclemod.entity.parts;

public class PartSlot {

    private final String slotId; // ex: "engine_bay", "wheel_front_left"
    private PartState state;
    private VehiclePart installedPart;

    public PartSlot(String slotId) {
        this.slotId = slotId;
        this.state = PartState.EMPTY;
        this.installedPart = null;
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

    public boolean isSecured() {
        return this.state == PartState.SECURED;
    }

    public boolean isEmpty() {
        return this.state == PartState.EMPTY;
    }

    public VehiclePart getPart() {
        return this.installedPart;
    }

    public String getSlotId() {
        return this.slotId;
    }
}
