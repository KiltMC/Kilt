package xyz.bluspring.kilt.forgeinjects.data.registries;

import net.minecraft.core.Registry;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.data.registries.VanillaRegistriesInjection;

import java.util.List;

@Mixin(VanillaRegistries.class)
public class VanillaRegistriesInject {
    @CreateStatic
    private static final List<? extends ResourceKey<? extends Registry<?>>> DATAPACK_REGISTRY_KEYS = VanillaRegistriesInjection.DATAPACK_REGISTRY_KEYS;
}
