package fr.frankulinn.vehiclemod.client.gui;

import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;

public class VehicleHudOverlay implements LayeredDraw.Layer {

    // Une instance unique pour l'enregistrer facilement
    public static final VehicleHudOverlay INSTANCE = new VehicleHudOverlay();

    @Override
    public void render(GuiGraphics guiGraphics, net.minecraft.client.DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        // On vérifie si le joueur existe et s'il est au volant de NOTRE véhicule
        if (player != null && player.getVehicle() instanceof VehicleEntity vehicle) {

            // --- 1. CALCULS ---
            // La vitesse (Blocks par tick -> km/h)
            double speedBpt = vehicle.getDeltaMovement().horizontalDistance();
            int speedKmh = (int) Math.round(speedBpt * 72.0);

            // L'essence (Pourcentage)
            float currentFuel = vehicle.getEntityData().get(VehicleEntity.FUEL_LEVEL);
            int fuelPercentage = (int) ((currentFuel / VehicleEntity.MAX_FUEL) * 100);

            // --- 2. POSITION SUR L'ÉCRAN ---
            // On le met en bas à droite de l'écran
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();

            int x = screenWidth - 110; // 110 pixels depuis le bord droit
            int y = screenHeight - 40; // 40 pixels depuis le bas

            // --- 3. DESSIN ---
            // Un petit fond noir semi-transparent pour bien lire le texte
            guiGraphics.fill(x - 5, y - 5, screenWidth - 5, screenHeight - 5, 0x80000000);

            // Le texte de la vitesse (En blanc)
            guiGraphics.drawString(minecraft.font, "Vitesse : " + speedKmh + " km/h", x, y, 0xFFFFFF);

            // Le texte de l'essence (Devient rouge si on a moins de 20%)
            int fuelColor = fuelPercentage < 20 ? 0xFF5555 : 0x55FF55; // Rouge clair / Vert clair
            guiGraphics.drawString(minecraft.font, "Essence : " + fuelPercentage + " %", x, y + 15, fuelColor);
        }
    }
}
