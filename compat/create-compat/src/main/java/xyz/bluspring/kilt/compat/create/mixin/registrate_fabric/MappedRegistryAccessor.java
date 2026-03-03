package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor<T> {

    @Accessor
    Map<T, Holder.Reference<T>> getUnregisteredIntrusiveHolders();

}
