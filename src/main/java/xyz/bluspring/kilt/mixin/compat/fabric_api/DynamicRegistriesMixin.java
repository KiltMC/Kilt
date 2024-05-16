package xyz.bluspring.kilt.mixin.compat.fabric_api;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraftforge.registries.DataPackRegistriesHooks;
import org.jetbrains.annotations.Unmodifiable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(value = DynamicRegistries.class, remap = false)
public class DynamicRegistriesMixin {
    @WrapOperation(method = "getDynamicRegistries", at = @At(value = "INVOKE", target = "Lnet/fabricmc/fabric/impl/registry/sync/DynamicRegistriesImpl;getDynamicRegistries()Ljava/util/List;"))
    private static @Unmodifiable List<RegistryDataLoader.RegistryData<?>> kilt$addToDynamicRegistries(Operation<List<RegistryDataLoader.RegistryData<?>>> original) {
        return Stream.concat(original.call().stream(), DataPackRegistriesHooks.getDataPackRegistries().stream()).collect(Collectors.toSet()).stream().toList();
    }
}
