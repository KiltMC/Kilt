package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.level.BlockGetter;
import net.minecraftforge.common.extensions.IForgeBlockGetter;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockGetter.class)
public interface BlockGetterInject extends IForgeBlockGetter {
}
