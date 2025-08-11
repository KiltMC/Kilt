package xyz.bluspring.kilt.injects.resources;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.resources.RegistryOpsInjection;

@Mixin(RegistryOps.class)
public abstract class RegistryOpsInject {
    @CreateStatic
    private static <E> MapCodec<HolderLookup.RegistryLookup<E>> retrieveRegistryLookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return RegistryOpsInjection.retrieveRegistryLookup(registryKey);
    }
}
