package xyz.bluspring.kilt.compat.forge.mixin.cyclopscore;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@IfModLoaded("cyclopscore")
@Pseudo
@Mixin(targets = "org.cyclops.cyclopscore.init.ModBase")
public class ModBaseMixin {
    @ModifyExpressionValue(method = "beforeRegistriedFilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceKey;equals(Ljava/lang/Object;)Z"))
    private boolean kilt$fixDifferentRegistryOrder(boolean original, RegisterEvent event) {
        return event.getRegistryKey().equals(ForgeRegistries.PARTICLE_TYPES.getRegistryKey());
    }
}
