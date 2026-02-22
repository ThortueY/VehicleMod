package fr.frankulinn.vehiclemod.client.renderer;


import com.mojang.blaze3d.vertex.PoseStack;
import fr.frankulinn.vehiclemod.client.model.VehicleChassisModel;
import fr.frankulinn.vehiclemod.client.renderer.layer.VehicleHitboxLayer;
import fr.frankulinn.vehiclemod.client.renderer.layer.VehiclePartsLayer;
import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class VehicleChassisRenderer extends GeoEntityRenderer<BaseVehicleEntity> {

    public VehicleChassisRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new VehicleChassisModel());

        // Le calque qui dessine les modèles 3D des pièces
        this.addRenderLayer(new VehiclePartsLayer(this));

        // --- NOUVEAU : Le calque qui dessine les hologrammes ---
        this.addRenderLayer(new VehicleHitboxLayer(this));

        this.shadowRadius = 0.5f;
    }

    @Override
    public void render(BaseVehicleEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        // 1. On sauvegarde l'état actuel de la caméra
        poseStack.pushPose();

        // 2. Calcul fluide de la rotation (le Lerp empêche la voiture de saccader quand elle tourne)
        float lerpYaw = net.minecraft.util.Mth.lerp(partialTick, entity.yRotO, entity.getYRot());

        // 3. On tourne TOUT l'espace de rendu (Châssis + Pièces)
        // ⚠️ NOTE : "180.0f - lerpYaw" est la norme dans Minecraft pour remettre les modèles à l'endroit.
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-lerpYaw));

        // 4. On laisse GeckoLib dessiner la voiture dans cet espace maintenant tourné
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        // 5. On restaure la caméra pour ne pas faire tourner tout le reste du jeu !
        poseStack.popPose();
    }
}
