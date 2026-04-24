package xyz.bluspring.kilt.injects.data.registries;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.neoforged.neoforge.registries.DataPackRegistriesHooks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.RegistryDataLoader;

@Mixin(RegistryPatchGenerator.class)
public abstract class RegistryPatchGeneratorInject {
    @ModifyExpressionValue(method = "method_54839", at = @At(value = "FIELD", target = "Lnet/minecraft/resources/RegistryDataLoader;WORLDGEN_REGISTRIES:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<RegistryDataLoader.RegistryData<?>> kilt$mergeDataPackRegistries(List<RegistryDataLoader.RegistryData<?>> original) {
        var set = new HashSet<RegistryDataLoader.RegistryData<?>>();
        set.addAll(original);
        set.addAll(DataPackRegistriesHooks.getDataPackRegistries());
        return new ArrayList<>(set);
    }
}
