package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public abstract class SwordItemInject extends TieredItem {
    public SwordItemInject(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SWORD_ACTIONS.contains(toolAction);
    }
}
