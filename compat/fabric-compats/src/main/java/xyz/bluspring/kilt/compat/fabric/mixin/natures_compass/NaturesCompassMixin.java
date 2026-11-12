package xyz.bluspring.kilt.compat.fabric.mixin.natures_compass;

import com.chaosthedude.naturescompass.NaturesCompass;
import com.chaosthedude.naturescompass.items.NaturesCompassItem;
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
@IfModLoaded("naturescompass")
@Mixin(NaturesCompass.class)
public abstract class NaturesCompassMixin {
    @Shadow
    @Final
    public static NaturesCompassItem NATURES_COMPASS_ITEM;

    @CreateStatic
    private static NaturesCompassItem naturesCompass;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$naturescompass$fixNeoFuckery(CallbackInfo ci) {
        naturesCompass = NATURES_COMPASS_ITEM;
    }
}
