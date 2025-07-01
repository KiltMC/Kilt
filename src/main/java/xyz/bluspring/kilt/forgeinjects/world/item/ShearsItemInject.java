package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShearsItem.class)
public abstract class ShearsItemInject extends Item {
    public ShearsItemInject(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, io.github.fabricators_of_create.porting_lib.util.ToolAction toolAction) {
        return io.github.fabricators_of_create.porting_lib.util.ToolActions.DEFAULT_SHEARS_ACTIONS.contains(toolAction);
    }
}
