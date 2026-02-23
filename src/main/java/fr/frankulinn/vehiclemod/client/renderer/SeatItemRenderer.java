package fr.frankulinn.vehiclemod.client.renderer;

import fr.frankulinn.vehiclemod.client.model.SeatItemModel;
import fr.frankulinn.vehiclemod.item.SeatItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SeatItemRenderer extends GeoItemRenderer<SeatItem> {
    public SeatItemRenderer() {
        super(new SeatItemModel());
    }
}
