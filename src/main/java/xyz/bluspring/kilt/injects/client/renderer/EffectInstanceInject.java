package xyz.bluspring.kilt.injects.client.renderer;

import net.minecraft.client.renderer.EffectInstance;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EffectInstance.class)
public abstract class EffectInstanceInject {
    // Kilt: Handled by Architectury
    /*@Shadow @Final private static String EFFECT_SHADER_PATH;

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$tryLoadNamespacedEffectInstance(String location, Operation<ResourceLocation> original, @Local(argsOnly = true) String fullLocation) {
        if (fullLocation.contains(":")) {
            var rl = ResourceLocation.tryParse(fullLocation);

            if (rl != null) {
                return new ResourceLocation(rl.getNamespace(), EFFECT_SHADER_PATH + rl.getPath() + ".json");
            }
        }

        return original.call(location);
    }

    @WrapOperation(method = "getOrCreate", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation kilt$tryLoadNamespacedEffectInstance(String location, Operation<ResourceLocation> original, @Local(argsOnly = true) String fullLocation, @Local(argsOnly = true) Program.Type programType) {
        if (fullLocation.contains(":")) {
            var rl = ResourceLocation.tryParse(fullLocation);

            if (rl != null) {
                return new ResourceLocation(rl.getNamespace(), EFFECT_SHADER_PATH + rl.getPath() + programType.getExtension());
            }
        }

        return original.call(location);
    }*/
}
