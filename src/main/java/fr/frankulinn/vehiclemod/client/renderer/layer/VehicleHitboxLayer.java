package fr.frankulinn.vehiclemod.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import fr.frankulinn.vehiclemod.entity.VehicleEntity;
import fr.frankulinn.vehiclemod.entity.parts.PartSlot;
import fr.frankulinn.vehiclemod.item.EngineItem;
import fr.frankulinn.vehiclemod.item.JerricanItem;
import fr.frankulinn.vehiclemod.item.WheelItem;
import fr.frankulinn.vehiclemod.item.WrenchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class VehicleHitboxLayer extends GeoRenderLayer<VehicleEntity> {

    public VehicleHitboxLayer(GeoEntityRenderer<VehicleEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, VehicleEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {

        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        // 1. On ne dessine que si le joueur est proche (ex: à moins de 6 blocs)
        if (player.distanceToSqr(animatable) > 36.0) return;

        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.isEmpty()) return;

        Item item = heldItem.getItem();
        boolean isEngine = item instanceof EngineItem;
        boolean isWheel = item instanceof WheelItem;
        boolean isWrench = item instanceof WrenchItem;
        boolean isJerrican = item instanceof JerricanItem;

        // Si on ne tient rien qui a un rapport avec la voiture, on arrête là
        if (!isEngine && !isWheel && !isWrench && !isJerrican) return;

        // On prépare le pinceau pour dessiner des lignes
        VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lines());

        // 2. On boucle sur tous les emplacements de la voiture
        for (PartSlot slot : animatable.getPartSlots()) {
            boolean shouldHighlight = false;
            float r = 0f, g = 1f, b = 0f; // Vert par défaut

            // 3. LOGIQUE D'AFFICHAGE SELON L'ITEM ET L'ÉTAT DU SLOT
            if (slot.isEmpty()) {
                if (isEngine && slot.getId().equals("engine_bay")) shouldHighlight = true;
                if (isWheel && slot.getId().startsWith("wheel_")) shouldHighlight = true;
            } else {
                if (isWrench && !slot.isSecured()) {
                    shouldHighlight = true;
                    r = 1f; g = 1f; b = 0f; // Jaune (Besoin d'être vissé)
                } else if (isWrench && slot.isSecured()) {
                    shouldHighlight = true;
                    r = 1f; g = 0.5f; b = 0f; // Orange (Peut être dévissé)
                }
            }

            if (isJerrican && slot.getId().equals("fuel_cap")) {
                shouldHighlight = true;
                r = 0f; g = 1f; b = 1f; // Cyan (Trappe à essence)
            }

            // 4. LE DESSIN DU CARRÉ
            if (shouldHighlight) {
                Vec3 offset = slot.getOffset();
                float hw = slot.getHitboxWidth() / 2.0f;
                float hh = slot.getHitboxHeight() / 2.0f;

                poseStack.pushPose();

                // On se place sur la hitbox (avec notre fameuse inversion X et Z !)
                poseStack.translate(offset.x, offset.y, offset.z);

                // On crée une boîte virtuelle
                AABB box = new AABB(-hw, 0, -hw, hw, slot.getHitboxHeight(), hw);

                // On dessine la boîte avec les couleurs choisies
                LevelRenderer.renderLineBox(poseStack, lineBuffer, box, r, g, b, 1.0F);

                poseStack.popPose();
            }
        }
    }
}
