package xyz.bluspring.kilt.mixin.core;

import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

@Mixin(BuiltInRegistries.class)
public interface BuiltInRegistriesAccessor {
    @Accessor("LOADERS")
    static Map<Identifier, Supplier<?>> getLoaders() {
        throw new IllegalStateException();
    }
}
