package xyz.bluspring.kilt.forgeinjects.gametest.framework;

import net.minecraft.gametest.framework.GameTest;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.gametest.framework.GameTestInjection;

@Mixin(GameTest.class)
public interface GameTestInject extends GameTestInjection {
}
