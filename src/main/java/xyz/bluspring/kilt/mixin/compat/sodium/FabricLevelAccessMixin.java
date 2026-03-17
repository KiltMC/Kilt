package xyz.bluspring.kilt.mixin.compat.sodium;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.caffeinemc.mods.sodium.client.world.SodiumAuxiliaryLightManager;
import net.caffeinemc.mods.sodium.fabric.level.FabricLevelAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.core.SectionPos;
import net.minecraft.world.level.chunk.LevelChunk;

@IfModLoaded("sodium")
@Mixin(FabricLevelAccess.class)
public abstract class FabricLevelAccessMixin {
    /**
     * @author BluSpring
     * @reason We introduce the auxiliary light manager with Kilt.
     */
    @Overwrite
    public SodiumAuxiliaryLightManager getLightManager(LevelChunk chunk, SectionPos pos) {
        return (SodiumAuxiliaryLightManager) chunk.getAuxLightManager(pos.origin());
    }
}
