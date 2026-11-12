package xyz.bluspring.kilt.compat.fabric.mixin.explorers_compass;

import com.chaosthedude.explorerscompass.ExplorersCompass;
import com.chaosthedude.explorerscompass.items.ExplorersCompassItem;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Pseudo
@IfModLoaded("explorerscompass")
@Mixin(ExplorersCompass.class)
public abstract class ExplorersCompassMixin {
    @Shadow @Final public static ExplorersCompassItem EXPLORERS_COMPASS_ITEM;

    @CreateStatic private static ExplorersCompassItem explorersCompass;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$explorerscompass$fixNeoFuckery(CallbackInfo ci) {
        explorersCompass = EXPLORERS_COMPASS_ITEM;
    }
}
