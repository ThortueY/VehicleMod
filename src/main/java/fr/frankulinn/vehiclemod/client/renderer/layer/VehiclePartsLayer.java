package fr.frankulinn.vehiclemod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frankulinn.vehiclemod.client.model.EngineModel;
import fr.frankulinn.vehiclemod.client.model.WheelModel;
import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtil;

public class VehiclePartsLayer extends GeoRenderLayer<VehicleEntity> {

    // On instancie le modèle du moteur pour pouvoir le dessiner
    private final EngineModel engineModel = new EngineModel();
    private final WheelModel wheelModel = new WheelModel();

    public VehiclePartsLayer(GeoEntityRenderer<VehicleEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, VehicleEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        // --- 1. RENDU DU MOTEUR ---
        if (animatable.getEntityData().get(VehicleEntity.HAS_ENGINE)) {

            fr.frankulinn.vehiclemod.entity.parts.PartSlot engineSlot = animatable.getSlot("engine_bay");

            if (engineSlot != null && engineSlot.getOffset() != null) {
                net.minecraft.world.phys.Vec3 offset = engineSlot.getOffset();

                poseStack.pushPose();

                poseStack.translate(-offset.x, offset.y, offset.z);

                BakedGeoModel bakedEngineModel = this.engineModel.getBakedModel(this.engineModel.getModelResource(animatable));
                RenderType engineRenderType = RenderType.entityCutoutNoCull(this.engineModel.getTextureResource(animatable));
                VertexConsumer engineBuffer = bufferSource.getBuffer(engineRenderType);

                this.getRenderer().reRender(bakedEngineModel, poseStack, bufferSource, animatable, engineRenderType, engineBuffer, partialTick, packedLight, packedOverlay, 0xFFFFFFFF);

                poseStack.popPose();
            }
        } // <-- L'ACCOLADE DU MOTEUR SE FERME ICI

        // --- 2. RENDU DES ROUES ---
        // Elles sont maintenant indépendantes du moteur !
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_front_left", VehicleEntity.WHEEL_FL);
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_front_right", VehicleEntity.WHEEL_FR);
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_back_left", VehicleEntity.WHEEL_BL);
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_back_right", VehicleEntity.WHEEL_BR);
    }

    private void renderWheel(PoseStack poseStack, VehicleEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay, String slotId, net.minecraft.network.syncher.EntityDataAccessor<String> dataAccessor) {

        // On lit le type de roue via le réseau (ex: "offroad", "kart", ou "none")
        String wheelType = animatable.getEntityData().get(dataAccessor);

        if (!wheelType.equals("none")) {
            PartSlot slot = animatable.getSlot(slotId);

            if (slot != null && slot.getOffset() != null) {
                net.minecraft.world.phys.Vec3 offset = slot.getOffset();

                poseStack.pushPose();

                // 1. On déplace le pinceau sur la hitbox
                poseStack.translate(-offset.x, offset.y, offset.z);

                // 🔥 ASTUCE DE PRO :
                // Les roues du côté droit (X négatif) doivent être retournées de 180° pour que la jante soit vers l'extérieur !
                if (offset.x < 0) {
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));
                }

                // 2. On dit au modèle quel fichier charger
                this.wheelModel.setWheelType(wheelType);

                // 3. On prépare le modèle et la texture
                software.bernie.geckolib.cache.object.BakedGeoModel bakedModel = this.wheelModel.getBakedModel(this.wheelModel.getModelResource(animatable));
                RenderType renderType = RenderType.entityCutoutNoCull(this.wheelModel.getTextureResource(animatable));
                com.mojang.blaze3d.vertex.VertexConsumer buffer = bufferSource.getBuffer(renderType);

                // 4. On dessine !
                this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer, partialTick, packedLight, packedOverlay, 0xFFFFFFFF);

                poseStack.popPose();
            }
        }
    }

        // --- 2. RENDU DES ROUES ---
        // (On fera la même chose ici plus tard : on cherchera "mount_wheel_fl", on lira le WHEEL_FL, et on dessinera le WheelModel)

}
