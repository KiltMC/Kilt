package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.item.TooltipFlagInjection;

@Mixin(TooltipFlag.class)
public interface TooltipFlagInject extends TooltipFlagInjection {
    @Override
    default boolean hasControlDown() {
        return false;
    }

    @Override
    default boolean hasShiftDown() {
        return false;
    }

    @Override
    default boolean hasAltDown() {
        return false;
    }
}
