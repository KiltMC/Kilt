package xyz.bluspring.kilt.injects.core;

import com.mojang.serialization.Lifecycle;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.IRegistryExtension;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DefaultedMappedRegistry.class)
public abstract class DefaultedMappedRegistryInject<T> extends MappedRegistry<T> implements IRegistryExtension<T> {
    public DefaultedMappedRegistryInject(ResourceKey<? extends Registry<T>> key, Lifecycle registryLifecycle) {
        super(key, registryLifecycle);
    }

    @Override
    public @Nullable ResourceLocation getKeyOrNull(T element) {
        return super.getKey(element);
    }
}
