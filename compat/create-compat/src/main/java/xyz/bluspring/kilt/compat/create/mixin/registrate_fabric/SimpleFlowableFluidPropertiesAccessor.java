package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(com.tterrag.registrate.fabric.SimpleFlowableFluid.Properties.class)
public interface SimpleFlowableFluidPropertiesAccessor {
    @Accessor
    Supplier<? extends Fluid> getStill();

    @Accessor
    Supplier<? extends Fluid> getFlowing();

    @Accessor
    boolean isInfinite();

    @Accessor
    Supplier<? extends Item> getBucket();

    @Accessor
    Supplier<? extends LiquidBlock> getBlock();

    @Accessor
    int getFlowSpeed();

    @Accessor
    int getLevelDecreasePerBlock();

    @Accessor
    float getBlastResistance();

    @Accessor
    int getTickRate();
}
