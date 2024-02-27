// TRACKED HASH: a21056576fc73d9bb232df2f44147986d9f61768
package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.item.AxeItemInjection;

@Mixin(AxeItem.class)
public class AxeItemInject implements AxeItemInjection {
    @CreateStatic
    private static BlockState getAxeStrippingState(BlockState originalState) {
        return AxeItemInjection.getAxeStrippingState(originalState);
    }
}