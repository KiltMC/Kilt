package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.shaders.Program;
import kotlin.text.StringsKt;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.resources.ResourceLocation;

@Mixin(EffectInstance.class)
public abstract class EffectInstanceInject {
    @Shadow @Final private static String EFFECT_SHADER_PATH;

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$tryLoadNamespacedEffectInstance(String location, Operation<ResourceLocation> original, @Local(argsOnly = true) String fullLocation) {
        if (StringsKt.count(location, c -> c == ':') == 1) {
            var rl = ResourceLocation.tryParse(fullLocation);

            if (rl != null) {
                return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), EFFECT_SHADER_PATH + rl.getPath() + ".json");
            }
        }

        return original.call(location);
    }

    @WrapOperation(method = "getOrCreate", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation kilt$tryLoadNamespacedEffectInstance(String location, Operation<ResourceLocation> original, @Local(argsOnly = true) String fullLocation, @Local(argsOnly = true) Program.Type programType) {
        if (StringsKt.count(location, c -> c == ':') == 1) {
            var rl = ResourceLocation.tryParse(fullLocation);

            if (rl != null) {
                return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), EFFECT_SHADER_PATH + rl.getPath() + programType.getExtension());
            }
        }

        return original.call(location);
    }
}
