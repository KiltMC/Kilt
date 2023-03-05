package xyz.bluspring.kilt.mixin.porting_lib;

import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RegistryObject.class, remap = false)
public interface RegistryObjectAccessor<T> {
    @Invoker(remap = false)
    void callSetValue(T value);
}
