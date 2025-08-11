package xyz.bluspring.kilt.compat.fabric.mixin.sophisticatedcore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.neoforge.common.extensions.IForgeBlockState;
import net.p3pp3rf1y.sophisticatedcore.extensions.block.SophisticatedBlockState;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("sophisticatedcore")
@Mixin(IForgeBlockState.class)
public interface IForgeBlockStateMixin extends SophisticatedBlockState {
}
