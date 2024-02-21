package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.core.RegistrySetBuilderInjection;

import java.util.List;

@Mixin(RegistrySetBuilder.class)
public class RegistrySetBuilderInject implements RegistrySetBuilderInjection {
    @Shadow @Final public List<RegistrySetBuilder.RegistryStub<?>> entries;

    @Override
    public List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys() {
        return this.entries.stream().map(RegistrySetBuilder.RegistryStub::key).toList();
    }
}
