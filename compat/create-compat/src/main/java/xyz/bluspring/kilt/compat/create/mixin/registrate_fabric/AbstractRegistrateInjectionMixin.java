package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.compat.create.registrate.injects.AbstractRegistrateInjection;

@IfModLoaded("registrate-fabric")
@Mixin(AbstractRegistrateInjection.class) // Only here to force the token replacement to occur!
public interface AbstractRegistrateInjectionMixin {
}
