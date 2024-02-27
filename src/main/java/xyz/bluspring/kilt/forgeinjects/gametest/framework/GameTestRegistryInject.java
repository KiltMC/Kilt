// TRACKED HASH: f02e766e5f7ff541845462963634270b691f3cc9
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