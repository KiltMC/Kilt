package xyz.bluspring.kilt.mixin.compat.sodium;

import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.neoforged.neoforge.common.world.AuxiliaryLightManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AuxiliaryLightManager.class)
public class AuxiliaryLightManagerMixin implements SodiumAuxiliaryLightManager {
}
