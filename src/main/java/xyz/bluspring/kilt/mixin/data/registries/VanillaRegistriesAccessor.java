package xyz.bluspring.kilt.mixin.data.registries;

import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.data.registries.VanillaRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VanillaRegistries.class)
public interface VanillaRegistriesAccessor {
    @Accessor("BUILDER")
    static RegistrySetBuilder getBuilder() {
        throw new UnsupportedOperationException();
    }
}
