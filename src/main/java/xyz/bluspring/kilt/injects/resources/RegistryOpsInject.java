package xyz.bluspring.kilt.injects.resources;

import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.resources.RegistryOpsInjection;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.DelegatingOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

@Mixin(RegistryOps.class)
public abstract class RegistryOpsInject<T> extends DelegatingOps<T> {
    @Shadow @Final @Mutable private RegistryOps.RegistryInfoLookup lookupProvider;

    @CreateInitializer
    protected RegistryOpsInject(RegistryOps<T> other) {
        super(other);
        this.lookupProvider = other.lookupProvider;
    }

    @CreateStatic
    private static <E> MapCodec<HolderLookup.RegistryLookup<E>> retrieveRegistryLookup(ResourceKey<? extends Registry<? extends E>> registryKey) {
        return RegistryOpsInjection.retrieveRegistryLookup(registryKey);
    }
}
