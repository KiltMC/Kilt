package xyz.bluspring.kilt.mixin.compat.owo;

import com.bawnorton.mixinsquared.TargetHandler;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("owo")
@Mixin(value = ShaderInstance.class, priority = 1550)
public class ShaderInstanceMixin {
    @TargetHandler(
            mixin = "xyz.bluspring.kilt.forgeinjects.client.renderer.ShaderInstanceInject",
            name = "kilt$addForgeSupportToFabricAPI"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$checkOwoProgram(String id, CallbackInfoReturnable<String> cir, CallbackInfo ci) {
        if (getClass().getName().equals("io.wispforest.owo.shader.GlProgram$OwoShaderProgram"))
            ci.cancel();
    }
}
