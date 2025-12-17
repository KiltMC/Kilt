package xyz.bluspring.kilt.compat.fabric.mixin.invmove;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import me.pieking1215.invmove.InvMove;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@IfModLoaded("invmove")
@Mixin(IForgeKeyMapping.class)
public interface IForgeKeyMappingMixin {
    @Inject(method = "isConflictContextAndModifierActive", at = @At("HEAD"), cancellable = true, remap = false)
    private void kilt$invmove$checkShouldForceRawKeyDown(CallbackInfoReturnable<Boolean> cir) {
        if (InvMove.instance().shouldForceRawKeyDown()) {
            cir.setReturnValue(true);
        }
    }
}
