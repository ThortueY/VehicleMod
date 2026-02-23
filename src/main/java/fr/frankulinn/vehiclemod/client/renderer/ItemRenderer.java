package fr.frankulinn.vehiclemod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import fr.frankulinn.vehiclemod.client.model.ItemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Renderer GeckoLib générique réutilisable pour les items.
 * Applique une transformation dans les contextes GUI/FIXED via
 * {@link #applyGuiTransform(PoseStack)}.
 * Utilisable directement sans sous-classe : {@code new ItemRenderer<MonItem>()}
 *
 * @param <T> Le type d'item, doit implémenter {@link GeoItem}
 */
public class ItemRenderer<T extends Item & GeoItem> extends GeoItemRenderer<T> {

    /**
     * Constructeur par défaut — utilise {@link ItemModel} avec résolution
     * automatique des ressources.
     */
    public ItemRenderer() {
        super(new ItemModel<>());
    }

    public ItemRenderer(ItemModel<T> model) {
        super(model);
    }

    /**
     * Applique une transformation PoseStack dans les contextes GUI et FIXED.
     * Surcharger cette méthode pour personnaliser l'orientation dans l'inventaire.
     * Ne rien faire ici par défaut (pas de transformation supplémentaire).
     *
     * @param poseStack la matrice de transformation courante
     */
    protected void applyGuiTransform(PoseStack poseStack) {
        poseStack.translate(0.5, 0.25, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(30));
        poseStack.mulPose(Axis.YP.rotationDegrees(225));
        poseStack.translate(-0.5, -0.5, -0.5);
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext transformType, PoseStack poseStack,
            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (transformType == ItemDisplayContext.GUI || transformType == ItemDisplayContext.FIXED) {
            poseStack.pushPose();
            applyGuiTransform(poseStack);
            super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
            poseStack.popPose();
        } else {
            super.renderByItem(stack, transformType, poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
