package xyz.bluspring.kilt.compat.sodium.mixin.cyclopscore;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.cyclops.cyclopscore.init.ModBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ModBase.class, remap = false)
public class ModBaseMixin {
    @ModifyExpressionValue(method = "beforeRegistriedFilled", at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceKey;equals(Ljava/lang/Object;)Z"))
    private boolean kilt$fixDifferentRegistryOrder(boolean original, RegisterEvent event) {
        return event.getRegistryKey().equals(ForgeRegistries.PARTICLE_TYPES.getRegistryKey());
    }
}
