package xyz.bluspring.kilt.injects.world.level.levelgen.feature.trunkplacers;

import java.util.function.BiConsumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

@Mixin(TrunkPlacer.class)
public abstract class TrunkPlacerInject {
    @Inject(method = "placeBelowTrunkBlock", at = @At("HEAD"), cancellable = true)
    private static void kilt$checkTreeGrowEvent(WorldGenLevel level, BiConsumer<BlockPos, BlockState> trunkSetter, RandomSource random, BlockPos pos, TreeConfiguration config, CallbackInfo ci) {
        if (level.getBlockState(pos).onTreeGrow(level, trunkSetter, random, pos, config)) {
            ci.cancel();
        }
    }
}
