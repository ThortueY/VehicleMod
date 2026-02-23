package fr.frankulinn.vehiclemod.client.model;

public enum ModelSubPath {

    ENTITY_ENGINE("entity/vehicle_engines"),
    ENTITY_SEATS("entity/vehicle_seats"),
    ENTITY_WHEELS("entity/vehicle_wheels"),
    ENTITY_CHASSIS("entity/vehicle_chassis"),
    ITEM("item/");

    private final String subPath;

    ModelSubPath(String subPath) {
        this.subPath = subPath;
    }

    public String getSubPath() {
        return subPath;
    }
}
