package xyz.bluspring.kilt.compat.forge.mixin.kotlinforforge;

import java.util.Optional;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thedarkcolour.kotlinforforge.neoforge.KotlinModContainer;

@IfModLoaded("kotlinforforge")
@Pseudo
@Mixin(value = KotlinModContainer.class, remap = false)
public abstract class KotlinModContainerMixin {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Optional;orElseThrow()Ljava/lang/Object;"))
    private <T> T kilt$kff$tryUseEmptyModuleLayer(Optional<T> instance) {
        return instance.orElse((T) this.getClass().getModule());
    }
}
