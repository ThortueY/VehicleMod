package fr.frankulinn.vehiclemod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frankulinn.vehiclemod.client.model.EngineModel;
import fr.frankulinn.vehiclemod.entity.VehicleEntity;
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

    public VehiclePartsLayer(GeoEntityRenderer<VehicleEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, VehicleEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        if (animatable.getEntityData().get(VehicleEntity.HAS_ENGINE)) {

            // 1. On récupère les coordonnées de la hitbox du moteur !
            fr.frankulinn.vehiclemod.entity.parts.PartSlot engineSlot = animatable.getSlot("engine_bay");

            if (engineSlot != null && engineSlot.getOffset() != null) {
                net.minecraft.world.phys.Vec3 offset = engineSlot.getOffset();

                poseStack.pushPose();

                // 2. On déplace le modèle aux coordonnées exactes de la hitbox !
                // NOTE : Dans Minecraft/GeckoLib, l'axe X est souvent inversé (-offset.x) pour les modèles.
                // L'axe Y est la hauteur, l'axe Z est la profondeur.
                poseStack.translate(-offset.x, offset.y, offset.z);

                BakedGeoModel bakedEngineModel = this.engineModel.getBakedModel(this.engineModel.getModelResource(animatable));
                RenderType engineRenderType = RenderType.entityCutoutNoCull(this.engineModel.getTextureResource(animatable));
                VertexConsumer engineBuffer = bufferSource.getBuffer(engineRenderType);

                this.getRenderer().reRender(bakedEngineModel, poseStack, bufferSource, animatable, engineRenderType, engineBuffer, partialTick, packedLight, packedOverlay, 0xFFFFFFFF);

                poseStack.popPose();
            }
        }
    }

        // --- 2. RENDU DES ROUES ---
        // (On fera la même chose ici plus tard : on cherchera "mount_wheel_fl", on lira le WHEEL_FL, et on dessinera le WheelModel)

}
