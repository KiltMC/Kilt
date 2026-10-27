package xyz.bluspring.kilt.injects.client.renderer.block;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.renderer.block.BlockModelLighterInjection;

import net.minecraft.client.renderer.block.BlockModelLighter;

@Mixin(BlockModelLighter.class)
public abstract class BlockModelLighterInject implements BlockModelLighterInjection {
    @Override
    public void reset() {
    }
}
