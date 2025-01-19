package xyz.bluspring.kilt.forgeinjects;

import net.minecraft.Util;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Util.class)
public abstract class UtilInject {
    @Redirect(method = "doFetchChoiceType", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;)V"))
    private static void kilt$useDebugForDataFixerError(Logger instance, String s, Object o) {
        instance.debug(s, o);
    }
}
