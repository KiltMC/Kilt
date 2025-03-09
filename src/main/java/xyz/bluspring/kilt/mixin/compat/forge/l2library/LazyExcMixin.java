package xyz.bluspring.kilt.mixin.compat.forge.l2library;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@IfModLoaded("l2library")
@Pseudo
@Mixin(targets = "dev.xkmc.l2library.util.code.LazyExc", remap = false)
public class LazyExcMixin {
    @WrapMethod(method = "get")
    private <T> T kilt$filterRemoveSynthetics(Operation<T> original) {
        try {
            return original.call();
        } catch (AssertionError e) { // I'm pretty sure this isn't actually supposed to cause a hard crash, but Forge eats it I guess.
            return null;
        }
    }
}
