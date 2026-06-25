package xyz.bluspring.kilt.injects.world.item;

import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.SpyglassItem;

@Mixin(SpyglassItem.class)
public abstract class SpyglassItemInject extends Item {
    public SpyglassItemInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_SPYGLASS_ACTIONS.contains(itemAbility);
    }
}
