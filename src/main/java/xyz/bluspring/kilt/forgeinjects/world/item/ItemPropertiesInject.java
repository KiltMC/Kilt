package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.item.ItemPropertiesInjection;

@Mixin(Item.Properties.class)
public class ItemPropertiesInject implements ItemPropertiesInjection {
    private boolean canRepair = true;

    @Override
    public Item.Properties setNoRepair() {
        canRepair = false;
        return (Item.Properties) (Object) this;
    }

    @Override
    public boolean getCanRepair() {
        return canRepair;
    }
}
