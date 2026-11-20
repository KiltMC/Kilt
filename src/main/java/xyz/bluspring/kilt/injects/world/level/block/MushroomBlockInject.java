package xyz.bluspring.kilt.injects.world.level.block;

import java.util.Optional;

import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

@Mixin(MushroomBlock.class)
public abstract class MushroomBlockInject extends VegetationBlock {
    protected MushroomBlockInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z"), cancellable = true)
    private void kilt$handleSoilDecision(BlockState state, LevelReader level, BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local(name = "below") BlockState soilState, @Local(name = "belowPos") BlockPos soilPos) {
        var soilDecision = state.canSustainPlant(level, soilPos, Direction.UP, soilState);

        if (!soilDecision.isDefault()) {
            cir.setReturnValue(soilDecision.isTrue());
        }
    }

    @ModifyVariable(method = "growMushroom", at = @At(value = "INVOKE", target = "Ljava/util/Optional;isEmpty()Z"), name = "feature")
    private Optional<? extends Holder<ConfiguredFeature<?, ?>>> kilt$handleFireBlockGrowFeature(Optional<? extends Holder<ConfiguredFeature<?, ?>>> optional, @Local(argsOnly = true, name = "level") ServerLevel level, @Local(argsOnly = true, name = "random") RandomSource random, @Local(argsOnly = true, name = "pos") BlockPos pos, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        var event = EventHooks.fireBlockGrowFeature(level, random, pos, optional.orElse(null));

        if (event.isCanceled()) {
            cir.setReturnValue(false);
        }

        return Optional.ofNullable(event.getFeature());
    }
}
