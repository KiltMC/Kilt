package xyz.bluspring.kilt.mixin.world.item;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.Kilt;

@Mixin(CreativeModeTab.ItemDisplayBuilder.class)
public class CreativeModeTabMixin {
    @Inject(method = "accept", at = @At(value = "INVOKE", target = "Ljava/lang/IllegalArgumentException;<init>(Ljava/lang/String;)V", shift = At.Shift.BEFORE), cancellable = true)
    private void kilt$avoidCreativeModeCrash(ItemStack stack, CreativeModeTab.TabVisibility tabVisibility, CallbackInfo ci) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Kilt.Companion.getLogger().error("An entry failed to load!");
            StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).forEach(f -> {
                Kilt.Companion.getLogger().error(f.toStackTraceElement().toString());
            });
            ci.cancel();
        }
    }
}
