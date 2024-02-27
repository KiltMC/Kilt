// TRACKED HASH: 15423649d9751f4ff18522351f8924289c6241df
package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.extensions.IForgeBlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockAndTintGetter.class)
public interface BlockAndTintGetterInject extends IForgeBlockAndTintGetter {
}