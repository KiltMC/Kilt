package xyz.bluspring.kilt.injects.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.pipeline.RenderTarget;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;

@Mixin(PostChain.class)
public abstract class PostChainInject {
    @Shadow @Final private RenderTarget screenTarget;

    @WrapOperation(method = "parsePassNode", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;withDefaultNamespace(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"), require = 0)
    private ResourceLocation kilt$tryLoadNamespacedPostChain(String location, Operation<ResourceLocation> original, @Local(ordinal = 5) String fullLocation) {
        if (fullLocation.contains(":")) {
            var rl = ResourceLocation.tryParse(fullLocation);

            if (rl != null) {
                return ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), "textures/effect/" + rl.getPath() + ".png");
            }
        }

        return original.call(location);
    }

    @Inject(method = "addTempTarget", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;setClearColor(FFFF)V", shift = At.Shift.AFTER))
    public void port_lib$isStencil(String name, int width, int height, CallbackInfo ci, @Local RenderTarget target) {
        if (this.screenTarget.isStencilEnabled()) {
            target.enableStencil();
        }
    }
}
