// TRACKED HASH: 1cee75da0c547cabd70aea9baffe3501be3da640
package xyz.bluspring.kilt.injects.world.level.block.state;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.extensions.IForgeBlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockState.class)
public class BlockStateInject implements IForgeBlockState {
}