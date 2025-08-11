package xyz.bluspring.kilt.compat.fabric.mixin.sophisticatedcore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforge.common.extensions.IForgeBlock;
import net.p3pp3rf1y.sophisticatedcore.extensions.block.SophisticatedBlock;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("sophisticatedcore")
@Mixin(IForgeBlock.class)
public interface IForgeBlockMixin extends SophisticatedBlock {
}
