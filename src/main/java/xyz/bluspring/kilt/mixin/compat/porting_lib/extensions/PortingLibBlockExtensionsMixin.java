package xyz.bluspring.kilt.mixin.compat.porting_lib.extensions;

import io.github.fabricators_of_create.porting_lib.blocks.extensions.BeaconColorMultiplierBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.ChunkUnloadListeningBlockEntity;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CollisionExtendsVerticallyBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.ConnectableRedstoneBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomBurnabilityBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomDestroyEffectsBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomDisplayFluidOverlayBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomExpBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomFrictionBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomHitEffectsBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomLadderBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomLandingEffectsBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomRailDirectionBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomRunningEffectsBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomScaffoldingBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomSlimeBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.CustomSoundTypeBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.EnchantmentBonusBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.EntityDestroyBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.ExplosionResistanceBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.FaceHidingBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.FireSourceBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.FlammableBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.HarvestableBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.LightEmissiveBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.MinecartPassHandlerBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.NeighborChangeListeningBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.OnExplodedBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.OnLoadBlockEntity;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.OnTreeGrowBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.PlayerDestroyBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.SlopeCreationCheckingRailBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.StateViewpointBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.StickToBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.StickyBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.SupportsClimbableOpenTrapdoorBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.VanillaCustomExpBlock;
import io.github.fabricators_of_create.porting_lib.blocks.extensions.WeakPowerCheckingBlock;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
    BeaconColorMultiplierBlock.class,
    ChunkUnloadListeningBlockEntity.class,
    CollisionExtendsVerticallyBlock.class,
    ConnectableRedstoneBlock.class,
    CustomBurnabilityBlock.class,
    CustomDestroyEffectsBlock.class,
    CustomDisplayFluidOverlayBlock.class,
    CustomExpBlock.class,
    CustomFrictionBlock.class,
    CustomHitEffectsBlock.class,
    CustomLadderBlock.class,
    CustomLandingEffectsBlock.class,
    CustomRailDirectionBlock.class,
    CustomRunningEffectsBlock.class,
    CustomScaffoldingBlock.class,
    CustomSlimeBlock.class,
    CustomSoundTypeBlock.class,
    EnchantmentBonusBlock.class,
    EntityDestroyBlock.class,
    ExplosionResistanceBlock.class,
    FaceHidingBlock.class,
    FireSourceBlock.class,
    FlammableBlock.class,
    HarvestableBlock.class,
    LightEmissiveBlock.class,
    MinecartPassHandlerBlock.class,
    NeighborChangeListeningBlock.class,
    OnExplodedBlock.class,
    OnLoadBlockEntity.class,
    OnTreeGrowBlock.class,
    PlayerDestroyBlock.class,
    SlopeCreationCheckingRailBlock.class,
    StateViewpointBlock.class,
    StickToBlock.class,
    StickyBlock.class,
    SupportsClimbableOpenTrapdoorBlock.class,
    VanillaCustomExpBlock.class,
    WeakPowerCheckingBlock.class,
})
public interface PortingLibBlockExtensionsMixin extends IBlockExtension {
}
