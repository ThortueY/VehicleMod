package fr.frankulinn.vehiclemod.client.renderer;

import fr.frankulinn.vehiclemod.client.model.WheelItemModel;
import fr.frankulinn.vehiclemod.item.WheelItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class WheelItemRenderer extends GeoItemRenderer<WheelItem> {
    public WheelItemRenderer() {
        super(new WheelItemModel());
    }
}
