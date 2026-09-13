package xyz.bluspring.kilt.compat.forge.mixin.cyclopscore;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@IfModLoaded("cyclopscore")
@Pseudo
@Mixin(targets = "org.cyclops.cyclopscore.init.ModBase")
public class ModBaseMixin {
//    @Dynamic
//    @ModifyExpressionValue(method = "beforeRegistriedFilled", at = @At(value = "INVOKE", target = "Ljava/lang/Object;equals(Ljava/lang/Object;)Z"))
//    private boolean kilt$fixDifferentRegistryOrder(boolean original, RegisterEvent event) {
//        return event.getRegistryKey().equals(Registries.PARTICLE_TYPE);
//    }
}
