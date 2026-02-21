package fr.frankulinn.vehiclemod.client.renderer;


import fr.frankulinn.vehiclemod.client.model.VehicleChassisModel;
import fr.frankulinn.vehiclemod.client.renderer.layer.VehiclePartsLayer;
import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VehicleChassisRenderer extends GeoEntityRenderer<VehicleEntity> {

    public VehicleChassisRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VehicleChassisModel());

        // 🔥 C'est LA seule ligne à ajouter ici !
        this.addRenderLayer(new VehiclePartsLayer(this));

        this.shadowRadius = 0.5f;
    }
}
