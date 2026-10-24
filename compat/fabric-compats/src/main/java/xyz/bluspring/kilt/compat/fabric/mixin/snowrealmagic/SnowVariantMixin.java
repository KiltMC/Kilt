package xyz.bluspring.kilt.compat.fabric.mixin.snowrealmagic;

import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import snownee.snow.block.SnowVariant;

@Pseudo
@Mixin(SnowVariant.class)
public interface SnowVariantMixin extends IBlockExtension {
}
