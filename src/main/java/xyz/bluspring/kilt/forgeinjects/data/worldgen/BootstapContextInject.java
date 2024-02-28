package xyz.bluspring.kilt.forgeinjects.data.worldgen;

import net.minecraft.data.worldgen.BootstapContext;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.data.worldgen.BootstapContextInjection;

@Mixin(BootstapContext.class)
public interface BootstapContextInject extends BootstapContextInjection {
}
