package xyz.bluspring.kilt.injects.network.protocol.configuration;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ServerboundSelectKnownPacks.class, priority = 1250)
public abstract class ServerboundSelectKnownPacksInject {
    @ModifyExpressionValue(method = "<clinit>", at = @At(value = "CONSTANT", args = "intValue=64"))
    private static int kilt$increaseKnownPacksValue(int original) {
        if (original > 64) // Kilt: prioritize other mixins
            return original;

        return 1024;
    }
}
