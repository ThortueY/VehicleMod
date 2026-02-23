package fr.frankulinn.vehiclemod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frankulinn.vehiclemod.client.model.EngineModel;
import fr.frankulinn.vehiclemod.client.model.SeatModel;
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
    private final SeatModel seatModel = new SeatModel();

    public VehiclePartsLayer(GeoEntityRenderer<BaseVehicleEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, BaseVehicleEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                       MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        // 1. On récupère notre dictionnaire de pièces envoyé par le serveur
        net.minecraft.nbt.CompoundTag syncedParts = animatable.getEntityData().get(BaseVehicleEntity.PARTS_SYNC);

        // 2. On boucle sur tous les slots prévus par le véhicule
        for (PartSlot slot : animatable.getPartSlots()) {

            // On regarde si le dictionnaire contient une pièce pour ce slot
            String modelId = syncedParts.getString(slot.getId());

            // Si la chaîne est vide ou vaut "none", ça veut dire qu'il n'y a rien de posé ici.
            if (modelId.isEmpty() || modelId.equals("none")) continue;

            poseStack.pushPose();
            Vec3 offset = slot.getOffset();

            // On place le "pinceau" à l'endroit du slot
            poseStack.translate(offset.x, offset.y, offset.z);

            // --- SI C'EST UNE ROUE ---
            if (slot.getId().startsWith("wheel")) {

                // Le braquage (Roues avant)
                if (slot.getId().contains("front")) {
                    float currentSteering = net.minecraft.util.Mth.lerp(partialTick, animatable.prevSteeringAngle, animatable.steeringAngle);
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-currentSteering));
                }

                // La rotation pour avancer et l'inversion Droite/Gauche
                float currentWheelRot = net.minecraft.util.Mth.lerp(partialTick, animatable.prevWheelRotation, animatable.wheelRotation);
                float currentWheelRotDegrees = currentWheelRot * (180F / (float)Math.PI);

                if (offset.x < 0) {
                    poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180f));
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-currentWheelRotDegrees)); // Utilise la valeur convertie !
                } else {
                    poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(currentWheelRotDegrees));  // Utilise la valeur convertie !
                }

                // On dessine la roue
                this.wheelModel.setWheelType(modelId);
                BakedGeoModel wheelBaked = this.wheelModel.getBakedModel(this.wheelModel.getModelResource(animatable));
                RenderType rtWheel = RenderType.entityCutoutNoCull(this.wheelModel.getTextureResource(animatable));
                this.getRenderer().reRender(wheelBaked, poseStack, bufferSource, animatable, rtWheel, bufferSource.getBuffer(rtWheel), partialTick, packedLight, packedOverlay, 0xFFFFFFFF);
            }

            // --- SI C'EST UN MOTEUR ---
            else if (slot.getId().startsWith("engine")) {
                this.engineModel.setEngineId(modelId);
                BakedGeoModel engineBaked = this.engineModel.getBakedModel(this.engineModel.getModelResource(animatable));
                RenderType rtEngine = RenderType.entityCutoutNoCull(this.engineModel.getTextureResource(animatable));
                this.getRenderer().reRender(engineBaked, poseStack, bufferSource, animatable, rtEngine, bufferSource.getBuffer(rtEngine), partialTick, packedLight, packedOverlay, 0xFFFFFFFF);
            }

            // --- SI C'EST UN SIÈGE ---
            else if (slot.getId().startsWith("seat")) {
                this.seatModel.setSeatId(modelId);
                BakedGeoModel seatBaked = this.seatModel.getBakedModel(this.seatModel.getModelResource(animatable));
                RenderType rtSeat = RenderType.entityCutoutNoCull(this.seatModel.getTextureResource(animatable));
                this.getRenderer().reRender(seatBaked, poseStack, bufferSource, animatable, rtSeat, bufferSource.getBuffer(rtSeat), partialTick, packedLight, packedOverlay, 0xFFFFFFFF);
            }

            poseStack.popPose();
        }
    }

    // --- 2. RENDU DES ROUES ---
    // (On fera la même chose ici plus tard : on cherchera "mount_wheel_fl", on lira
    // le WHEEL_FL, et on dessinera le WheelModel)

}
