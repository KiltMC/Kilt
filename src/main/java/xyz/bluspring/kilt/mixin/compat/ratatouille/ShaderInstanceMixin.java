package xyz.bluspring.kilt.mixin.compat.ratatouille;

import com.bawnorton.mixinsquared.TargetHandler;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("ratatouille")
@Mixin(value = ShaderInstance.class, priority = 1550)
public class ShaderInstanceMixin {
    @Unique private static final Class<?> kilt$ratatouilleShaderClass;

    static {
        try {
            kilt$ratatouilleShaderClass = Class.forName("dev.doctor4t.ratatouille.client.lib.render.systems.rendering.ExtendedShader");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /*@TargetHandler(
        mixin = "xyz.bluspring.kilt.forgeinjects.client.renderer.ShaderInstanceInject",
        name = "kilt$addForgeSupportToFabricAPI"
    )
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$checkOwoProgram(String id, CallbackInfoReturnable<String> cir, CallbackInfo ci) {
        if (kilt$ratatouilleShaderClass.isInstance(this))
            ci.cancel();
    }*/
}
