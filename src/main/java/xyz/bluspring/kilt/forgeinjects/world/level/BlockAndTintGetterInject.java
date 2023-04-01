package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraftforge.client.extensions.IForgeBlockAndTintGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockAndTintGetter.class)
public interface BlockAndTintGetterInject extends IForgeBlockAndTintGetter {
}
