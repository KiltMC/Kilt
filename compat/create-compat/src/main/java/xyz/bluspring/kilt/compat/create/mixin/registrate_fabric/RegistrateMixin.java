package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.Registrate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateForgeExtension;

@IfModLoaded("registrate-fabric")
@Mixin(Registrate.class)
public class RegistrateMixin {

    @ModifyReturnValue(method = "create", at = @At(value = "RETURN"), remap = false)
    private static Registrate create(Registrate original) {
        var registrate = ((AbstractRegistrateForgeExtension<Registrate>) original);
        if (Kilt.Companion.getLoader().hasMod(original.getModid())) {
            registrate.registerEventListeners(registrate.getModEventBus());
        }
        return original;
    }

}
