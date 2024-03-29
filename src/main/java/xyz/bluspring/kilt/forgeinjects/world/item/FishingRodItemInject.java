// TRACKED HASH: 62f3a843dc83d5df2702d12d71a6eaf49893cd00
package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = FishingRodItem.class, priority = 1010)
public class FishingRodItemInject {
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_FISHING_ROD_ACTIONS.contains(toolAction);
    }
}