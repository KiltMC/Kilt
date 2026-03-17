package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;

@IfModLoaded("sodium")
@Mixin(value = AuxiliaryLightManager.class, remap = false)
public interface AuxiliaryLightManagerMixin extends SodiumAuxiliaryLightManager {
}
