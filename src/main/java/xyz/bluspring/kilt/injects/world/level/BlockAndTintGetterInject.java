// TRACKED HASH: 15423649d9751f4ff18522351f8924289c6241df
package xyz.bluspring.kilt.injects.world.level;

import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.common.extensions.IBlockAndTintGetterExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockAndTintGetter.class)
public interface BlockAndTintGetterInject extends IBlockAndTintGetterExtension {
}