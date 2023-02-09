package xyz.bluspring.kilt.mixin;

import io.github.fabricators_of_create.porting_lib.util.LazyRegistrar;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(LazyRegistrar.class)
public interface LazyRegistrarAccessor<T> {
    @Accessor("entries")
    Map<RegistryObject<T>, Supplier<? extends T>> getEntrySet();
}
