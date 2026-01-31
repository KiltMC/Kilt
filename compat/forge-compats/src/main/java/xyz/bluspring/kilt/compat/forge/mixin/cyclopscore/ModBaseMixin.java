package xyz.bluspring.kilt.compat.forge.mixin.cyclopscore;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("cyclopscore")
@Pseudo
@Mixin(targets = "org.cyclops.cyclopscore.init.ModBase")
public class ModBaseMixin {
    @Dynamic
    @ModifyExpressionValue(method = "beforeRegistriedFilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceKey;equals(Ljava/lang/Object;)Z"))
    private boolean kilt$fixDifferentRegistryOrder(boolean original, RegisterEvent event) {
        return event.getRegistryKey().equals(Registries.PARTICLE_TYPE);
    }
}
