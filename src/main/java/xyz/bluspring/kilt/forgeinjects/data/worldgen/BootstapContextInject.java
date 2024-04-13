// TRACKED HASH: 50ccda2541c46785a0be46cf552b476164633abf
package xyz.bluspring.kilt.forgeinjects.data.worldgen;

import net.minecraft.data.worldgen.BootstapContext;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.data.worldgen.BootstapContextInjection;

@Mixin(BootstapContext.class)
public interface BootstapContextInject extends BootstapContextInjection {
}