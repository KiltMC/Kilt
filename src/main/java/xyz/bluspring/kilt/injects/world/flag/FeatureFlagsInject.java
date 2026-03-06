package xyz.bluspring.kilt.injects.world.flag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.flag.FeatureFlagRegistry;
import net.minecraft.world.flag.FeatureFlags;
import net.neoforged.neoforge.common.util.flag.FeatureFlagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FeatureFlags.class)
public abstract class FeatureFlagsInject {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/flag/FeatureFlagRegistry$Builder;build()Lnet/minecraft/world/flag/FeatureFlagRegistry;"))
    private static FeatureFlagRegistry kilt$registerModdedFlags(FeatureFlagRegistry.Builder instance, Operation<FeatureFlagRegistry> original) {
        FeatureFlagLoader.loadModdedFlags(instance);
        return original.call(instance);
    }
}
