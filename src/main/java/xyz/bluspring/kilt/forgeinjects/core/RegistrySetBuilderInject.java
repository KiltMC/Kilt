// TRACKED HASH: b2cb1e6d77ca3eafed7a519a77bc4a5b98e52c62
package xyz.bluspring.kilt.forgeinjects.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.core.RegistrySetBuilderInjection;

import java.util.List;

@Mixin(RegistrySetBuilder.class)
public class RegistrySetBuilderInject implements RegistrySetBuilderInjection {
    @Shadow @Final public List<RegistrySetBuilder.RegistryStub<?>> entries;

    @Override
    public List<? extends ResourceKey<? extends Registry<?>>> getEntryKeys() {
        return this.entries.stream().map(RegistrySetBuilder.RegistryStub::key).toList();
    }

    @Mixin(RegistrySetBuilder.BuildState.class)
    public static class BuildStateInject {
        @WrapOperation(method = "method_46790", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/RegistrySetBuilder;wrapContextLookup(Lnet/minecraft/core/HolderLookup$RegistryLookup;)Lnet/minecraft/core/HolderGetter;"))
        private static <T> HolderGetter<T> kilt$wrapRegistryLookup(HolderLookup.RegistryLookup<T> owner, Operation<HolderGetter<T>> original) {
            return ForgeHooks.wrapRegistryLookup(owner);
        }
    }
}