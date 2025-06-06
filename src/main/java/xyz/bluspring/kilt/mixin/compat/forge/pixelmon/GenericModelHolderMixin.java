package xyz.bluspring.kilt.mixin.compat.forge.pixelmon;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.lang.reflect.Constructor;
import java.util.Arrays;

@Pseudo
@Mixin(targets = "com.pixelmonmod.pixelmon.client.render.GenericModelHolder")
public abstract class GenericModelHolderMixin {
    // why would you ever willingly make your own mod code reflection-based. especially in rendering. that sounds like a performance nightmare.
    @ModifyExpressionValue(method = "loadModel", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getConstructors()[Ljava/lang/reflect/Constructor;"))
    private Constructor<?>[] kilt$pixelmon$getConstructorWithParams(Constructor<?>[] original) {
        return Arrays.stream(original).filter(e -> e.getParameterCount() > 0).toList().toArray(new Constructor[0]);
    }
}
