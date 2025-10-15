package xyz.bluspring.kilt.mixin.compat.sodium;

import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AuxiliaryLightManager.class, remap = false)
public interface AuxiliaryLightManagerMixin extends SodiumAuxiliaryLightManager {
}
