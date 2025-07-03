package xyz.bluspring.kilt.mixin.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(BuiltInRegistries.class)
public interface BuiltInRegistriesAccessor {
    @Accessor("LOADERS")
    static Map<ResourceLocation, Supplier<?>> getLoaders() {
        throw new IllegalStateException();
    }
}
