// TRACKED HASH: fe4695ec174902bd7838a48d51b89c86c234fb9f
package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.DeadBushBlock;
import net.neoforged.neoforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DeadBushBlock.class)
public abstract class DeadBushBlockInject implements IForgeShearable {
}