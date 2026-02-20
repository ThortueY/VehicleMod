package fr.frankulinn.vehiclemod.entity.parts;

public enum PartState {
    EMPTY,    // Aucun item dans l'emplacement
    PLACED,   // La pièce est posée à la main, mais pas fixée
    SECURED   // La pièce est vissée avec le bon outil (prête à rouler)
}
