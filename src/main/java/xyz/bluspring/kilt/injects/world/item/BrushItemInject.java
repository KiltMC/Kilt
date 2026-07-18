package xyz.bluspring.kilt.injects.world.item;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;

@Mixin(BrushItem.class)
public abstract class BrushItemInject extends Item {
    public BrushItemInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_BRUSH_ACTIONS.contains(itemAbility);
    }
}
