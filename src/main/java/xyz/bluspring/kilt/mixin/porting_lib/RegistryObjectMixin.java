package xyz.bluspring.kilt.mixin.porting_lib;

import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.porting_lib.RegistryObjectInjection;

import java.util.Optional;

@Mixin(value = RegistryObject.class, remap = false)
public abstract class RegistryObjectMixin<T> implements RegistryObjectInjection {
    @Shadow @Final private @Nullable ResourceKey<T> key;

    @Shadow @Final private ResourceLocation id;

    @Shadow abstract void setValue(T value);

    @Override
    public void updateRef() {
        if (this.key == null)
            return;

        Optional<T> optional = (Optional<T>) Registry.REGISTRY.get(this.key.registry()).getOptional(this.id);
        optional.ifPresent(this::setValue);
    }
}
