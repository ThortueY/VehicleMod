package fr.frankulinn.vehiclemod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frankulinn.vehiclemod.client.model.EngineModel;
import fr.frankulinn.vehiclemod.client.model.WheelModel;
import fr.frankulinn.vehiclemod.entity.BaseVehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class VehiclePartsLayer extends GeoRenderLayer<BaseVehicleEntity> {

    // On instancie le modèle du moteur pour pouvoir le dessiner
    private final EngineModel engineModel = new EngineModel();
    private final WheelModel wheelModel = new WheelModel();

    public VehiclePartsLayer(GeoEntityRenderer<BaseVehicleEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, BaseVehicleEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
            MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight,
            int packedOverlay) {

        // --- 1. RENDU DU MOTEUR ---
        if (animatable.getEntityData().get(BaseVehicleEntity.HAS_ENGINE)) {

            PartSlot engineSlot = animatable.getSlot("engine_bay");

            if (engineSlot != null && engineSlot.getOffset() != null) {
                Vec3 offset = engineSlot.getOffset();

                poseStack.pushPose();

                poseStack.translate(offset.x, offset.y, offset.z);

                this.engineModel.setEngineId(animatable.getEntityData().get(BaseVehicleEntity.ENGINE));
                BakedGeoModel bakedEngineModel = this.engineModel
                        .getBakedModel(this.engineModel.getModelResource(animatable));
                RenderType engineRenderType = RenderType
                        .entityCutoutNoCull(this.engineModel.getTextureResource(animatable));
                VertexConsumer engineBuffer = bufferSource.getBuffer(engineRenderType);

                this.getRenderer().reRender(bakedEngineModel, poseStack, bufferSource, animatable, engineRenderType,
                        engineBuffer, partialTick, packedLight, packedOverlay, 0xFFFFFFFF);

                poseStack.popPose();
            }
        } // <-- L'ACCOLADE DU MOTEUR SE FERME ICI

        // --- 2. RENDU DES ROUES ---
        // Elles sont maintenant indépendantes du moteur !
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_front_left",
                BaseVehicleEntity.WHEEL_FL);
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_front_right",
                BaseVehicleEntity.WHEEL_FR);
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_back_left",
                BaseVehicleEntity.WHEEL_BL);
        renderWheel(poseStack, animatable, bufferSource, partialTick, packedLight, packedOverlay, "wheel_back_right",
                BaseVehicleEntity.WHEEL_BR);
    }

    private void renderWheel(PoseStack poseStack, BaseVehicleEntity animatable, MultiBufferSource bufferSource,
            float partialTick, int packedLight, int packedOverlay, String slotId,
            EntityDataAccessor<String> dataAccessor) {

        String wheelType = animatable.getEntityData().get(dataAccessor);

        if (!wheelType.equals("none")) {
            PartSlot slot = animatable.getSlot(slotId);

            if (slot != null && slot.getOffset() != null) {
                Vec3 offset = slot.getOffset();

                poseStack.pushPose();

                // Le centre de la roue est maintenant à l'origine du modèle (Y=0)
                // grâce au recentrage de kart_wheel.geo.json
                float pivotX = 0.0f;
                float pivotY = 0.0f;
                float pivotZ = 0.0f;

                // 1. On déplace le pinceau à la position du slot
                // Le root bone du châssis a rotation Y=180°, ce qui inverse X et Z
                poseStack.translate(offset.x, offset.y, offset.z);

                float currentSteering = net.minecraft.util.Mth.lerp(partialTick, animatable.prevSteeringAngle,
                        animatable.steeringAngle);
                float currentWheelRot = net.minecraft.util.Mth.lerp(partialTick, animatable.prevWheelRotation,
                        animatable.wheelRotation);

                // On se déplace SUR le point de pivot de la roue avant de la tourner
                poseStack.translate(pivotX, pivotY, pivotZ);

                // 2. LE BRAQUAGE (Roues avant)
                if (slotId.contains("front")) {
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-currentSteering));
                }

                // 3. LA ROTATION ET L'INVERSION DROITE/GAUCHE
                if (offset.x < 0) {
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotation(-currentWheelRot));
                } else {
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotation(currentWheelRot));
                }

                // On revient en arrière après avoir tourné le pinceau !
                poseStack.translate(-pivotX, -pivotY, -pivotZ);

                // 4. On dessine !
                this.wheelModel.setWheelType(wheelType);
                BakedGeoModel bakedModel = this.wheelModel.getBakedModel(this.wheelModel.getModelResource(animatable));
                RenderType renderType = RenderType.entityCutoutNoCull(this.wheelModel.getTextureResource(animatable));
                VertexConsumer buffer = bufferSource.getBuffer(renderType);

                this.getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, renderType, buffer,
                        partialTick, packedLight, packedOverlay, 0xFFFFFFFF);

                poseStack.popPose();
            }
        }
    }

    // --- 2. RENDU DES ROUES ---
    // (On fera la même chose ici plus tard : on cherchera "mount_wheel_fl", on lira
    // le WHEEL_FL, et on dessinera le WheelModel)

}
