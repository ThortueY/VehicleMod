package fr.frankulinn.vehiclemod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import fr.frankulinn.vehiclemod.client.model.EngineItemModel;
import fr.frankulinn.vehiclemod.item.EngineItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class EngineItemRenderer extends GeoItemRenderer<EngineItem> {
    public EngineItemRenderer() {
        super(new EngineItemModel());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.FIXED) {
            poseStack.pushPose();
            // Centrer le modèle au milieu du slot
            poseStack.translate(0.5, 0.25, 0.5);
            // Rotation isométrique classique (comme les blocs Minecraft)
            poseStack.mulPose(Axis.XP.rotationDegrees(30));
            poseStack.mulPose(Axis.YP.rotationDegrees(225));
            poseStack.translate(-0.5, -0.5, -0.5);
            super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        } else {
            super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
