// TRACKED HASH: 33c37e1450e21ad82101b30b9cc1bf7f4cb0c12d
package xyz.bluspring.kilt.injects.core.registries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(BuiltInRegistries.class)
public abstract class BuiltInRegistriesInject {
    @Shadow @Final private static Map<ResourceLocation, Supplier<?>> LOADERS;

    @CreateStatic
    private static Set<ResourceLocation> getVanillaRegistrationOrder() {
        return Collections.unmodifiableSet(LOADERS.keySet());
    }
}