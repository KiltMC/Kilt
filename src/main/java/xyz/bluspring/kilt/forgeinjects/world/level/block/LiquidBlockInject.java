package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.block.LiquidBlockInjection;

@Mixin(LiquidBlock.class)
public class LiquidBlockInject implements LiquidBlockInjection {
    @Shadow @Final protected FlowingFluid fluid;

    @Override
    public FlowingFluid getFluid() {
        return this.fluid;
    }
}
