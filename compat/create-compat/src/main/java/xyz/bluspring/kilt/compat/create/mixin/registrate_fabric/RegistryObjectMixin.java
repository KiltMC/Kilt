package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.fabric.RegistryObject;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.create.extensions.RegistryObjectForgeExtension;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@IfModLoaded("registrate-fabric")
@Mixin(RegistryObject.class)
public abstract class RegistryObjectMixin<T> implements RegistryObjectForgeExtension<T> {
    @Shadow private @Nullable T object;

    @Shadow public abstract ResourceLocation getId();

    @Unique
    private ResourceKey<T> kilt$registryKey;

    @Override
    public void updateReference(@NotNull RegisterEvent event) {
        if (this.kilt$registryKey == null) {
            this.kilt$registryKey = ResourceKey.create((ResourceKey<? extends Registry<T>>) event.getRegistryKey(), getId());
        }
        if (event.getForgeRegistry() != null) {
            this.object = (T) event.getForgeRegistry().getValue(this.getId());
        } else {
            this.object = (T) event.getVanillaRegistry().get(this.getId());
        }
    }

    @Override
    public @NotNull ResourceKey<T> getKey() {
        return this.kilt$registryKey;
    }

    @Redirect(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/core/Registry;)V", at = @At(value = "INVOKE", target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object ignoreNullRegistry(Object o) {
        return o;
    }

    @WrapOperation(method = "<init>(Lnet/minecraft/resources/ResourceLocation;Lnet/minecraft/core/Registry;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Registry;get(Lnet/minecraft/resources/ResourceLocation;)Ljava/lang/Object;"))
    private Object ignoreNullRegistry(Registry instance, ResourceLocation resourceLocation, Operation<T> original) {
        if (instance == null)
            return null;
        kilt$registryKey = ResourceKey.create(instance.key(), resourceLocation);
        return original.call(instance, resourceLocation);
    }
}
