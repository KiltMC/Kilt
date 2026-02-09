package xyz.bluspring.kilt.injections.world.level.block;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.function.Supplier;

public interface LiquidBlockInjection {
    static LiquidBlock create(Supplier<? extends FlowingFluid> fluidSupplier, BlockBehaviour.Properties properties) {
        return new LiquidBlock(fluidSupplier.get(), properties);
    }

    default FlowingFluid getFluid() {
        throw KiltHelper.createMixinException(LiquidBlockInjection.class, "getFluid");
    }
}
