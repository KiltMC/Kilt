package xyz.bluspring.kilt.injections.gametest.framework;

import net.minecraft.gametest.framework.GameTestRegistry;

import java.lang.reflect.Method;
import java.util.Set;

public interface GameTestRegistryInjection {
    static void register(Method method, Set<String> allowedNamespaces) {
        GameTestRegistry.register(method);
    }
}
