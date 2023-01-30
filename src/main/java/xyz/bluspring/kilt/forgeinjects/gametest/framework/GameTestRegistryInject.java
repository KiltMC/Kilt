package xyz.bluspring.kilt.forgeinjects.gametest.framework;

import net.minecraft.gametest.framework.GameTestRegistry;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.gametest.framework.GameTestRegistryInjection;

@Mixin(GameTestRegistry.class)
public class GameTestRegistryInject implements GameTestRegistryInjection {
}
