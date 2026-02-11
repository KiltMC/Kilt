package xyz.bluspring.kilt.mixin.workarounds.avoid_feature_cycles;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(BiomeGenerationSettings.PlainBuilder.class)
public abstract class BiomeGenerationSettings$PlainBuilderMixin {
    @WrapOperation(method = "addFeature(ILnet/minecraft/core/Holder;)Lnet/minecraft/world/level/biome/BiomeGenerationSettings$PlainBuilder;", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    private <E> boolean kilt$preventFeatureReuse(List<E> instance, E e, Operation<Boolean> original) {
        if (instance.contains(e)) { // Prevent adding duplicate features.
            return false;
        }

        return original.call(instance, e);
    }
}
