package fr.frankulinn.vehiclemod.client.gui;

import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class VehicleHudOverlay implements LayeredDraw.Layer {

    // Une instance unique pour l'enregistrer facilement
    public static final VehicleHudOverlay INSTANCE = new VehicleHudOverlay();

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        // On vérifie si le joueur existe et s'il est au volant du véhicule
        if (player != null && player.getVehicle() instanceof BaseVehicleEntity vehicle) {

            // Calcul de la vitesse
            double speedBpt = vehicle.getDeltaMovement().horizontalDistance();
            int speedKmh = (int) Math.round(speedBpt * 72.0);

            // Calcul du niveau d'essence
            float currentFuel = vehicle.getEntityData().get(BaseVehicleEntity.FUEL_LEVEL);
            int fuelPercentage = (int) ((currentFuel / vehicle.getMaxFuel()) * 100);


            // position en bas à droite de l'écran
            int screenWidth = guiGraphics.guiWidth();
            int screenHeight = guiGraphics.guiHeight();

            int x = screenWidth - 110; // 110 pixels depuis le bord droit
            int y = screenHeight - 40; // 40 pixels depuis le bas

            // --- 3. DESSIN ---
            // fond noir
            guiGraphics.fill(x - 5, y - 5, screenWidth - 5, screenHeight - 5, 0x80000000);

            // Le texte de la vitesse
            guiGraphics.drawString(minecraft.font, "Vitesse : " + speedKmh + " km/h", x, y, 0xFFFFFF);

            // Le texte de l'essence (Devient rouge si on a moins de 20%)
            int fuelColor = fuelPercentage < 20 ? 0xFF5555 : 0x55FF55; // Rouge clair / Vert clair
            guiGraphics.drawString(minecraft.font, "Essence : " + fuelPercentage + " %", x, y + 15, fuelColor);
        }
    }
}
