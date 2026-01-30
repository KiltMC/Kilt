package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.level.block.BlockInjection;

@Mixin(Block.class)
public abstract class BlockInject implements BlockInjection {

}
