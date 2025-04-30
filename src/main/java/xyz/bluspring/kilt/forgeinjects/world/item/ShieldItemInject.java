package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public abstract class ShieldItemInject extends Item {
    public ShieldItemInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SHIELD_ACTIONS.contains(toolAction);
    }
}
