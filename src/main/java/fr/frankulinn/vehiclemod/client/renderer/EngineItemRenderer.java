package fr.frankulinn.vehiclemod.client.renderer;

import fr.frankulinn.vehiclemod.client.model.EngineItemModel;
import fr.frankulinn.vehiclemod.item.EngineItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class EngineItemRenderer extends GeoItemRenderer<EngineItem> {
    public EngineItemRenderer() {
        super(new EngineItemModel());
    }
}
