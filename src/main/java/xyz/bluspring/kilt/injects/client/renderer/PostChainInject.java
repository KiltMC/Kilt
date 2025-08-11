package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PostChain.class)
public abstract class PostChainInject {
    @WrapOperation(method = "parsePassNode", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation kilt$tryLoadNamespacedPostChain(String location, Operation<ResourceLocation> original, @Local(ordinal = 5) String fullLocation) {
        if (fullLocation.contains(":")) {
            var rl = ResourceLocation.tryParse(fullLocation);

            if (rl != null) {
                return new ResourceLocation(rl.getNamespace(), "textures/effect/" + rl.getPath() + ".png");
            }
        }

        return original.call(location);
    }
}
