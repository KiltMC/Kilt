package xyz.bluspring.kilt.compat.fabric.mixin.sophisticatedcore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.p3pp3rf1y.sophisticatedcore.extensions.block.SophisticatedBlock;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("sophisticatedcore")
@Mixin(IBlockExtension.class)
public interface IForgeBlockMixin extends SophisticatedBlock {
}
