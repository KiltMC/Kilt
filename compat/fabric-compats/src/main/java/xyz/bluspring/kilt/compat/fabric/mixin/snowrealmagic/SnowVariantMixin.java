package xyz.bluspring.kilt.compat.fabric.mixin.snowrealmagic;

import net.minecraftforge.common.extensions.IForgeBlock;
import org.spongepowered.asm.mixin.Mixin;
import snownee.snow.block.SnowVariant;

@Mixin(SnowVariant.class)
public interface SnowVariantMixin extends IForgeBlock {
}
