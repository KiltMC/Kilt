package xyz.bluspring.kilt.mixin;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RegistrySynchronization.NetworkedRegistryData.class)
public interface NetworkedRegistryDataAccessor {
    @Invoker("<init>")
    static <E> RegistrySynchronization.NetworkedRegistryData<E> createNetworkedRegistryData(ResourceKey<? extends Registry<E>> resourceKey, Codec<E> codec) {
        throw new UnsupportedOperationException();
    }
}
