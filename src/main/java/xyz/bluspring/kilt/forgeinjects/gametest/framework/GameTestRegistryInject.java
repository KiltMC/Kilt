package xyz.bluspring.kilt.forgeinjects.gametest.framework;

import net.minecraft.gametest.framework.GameTestRegistry;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.gametest.framework.GameTestRegistryInjection;

import java.lang.reflect.Method;
import java.util.Set;

@Mixin(GameTestRegistry.class)
public class GameTestRegistryInject implements GameTestRegistryInjection {
    @CreateStatic
    private static void register(Method method, Set<String> allowedNamespaces) {
        GameTestRegistryInjection.register(method, allowedNamespaces);
    }
}
