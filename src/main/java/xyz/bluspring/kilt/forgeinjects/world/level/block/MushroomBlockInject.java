package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.neoforged.bus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(MushroomBlock.class)
public abstract class MushroomBlockInject extends BushBlock {
    public MushroomBlockInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "canSurvive", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/MushroomBlock;mayPlaceOn(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kilt$checkCanSustainPlant(MushroomBlock instance, BlockState state, BlockGetter level, BlockPos pos, Operation<Boolean> original) {
        return original.call(instance, state, level, pos) || state.canSustainPlant(level, pos, Direction.UP, (IPlantable) this);
    }

    @Inject(method = "growMushroom", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"), cancellable = true)
    private void kilt$tryGrowFeatureEvent(ServerLevel level, BlockPos pos, BlockState state, RandomSource random, CallbackInfoReturnable<Boolean> cir, @Local Optional<? extends Holder<ConfiguredFeature<?, ?>>> optional, @Share("event") LocalRef<SaplingGrowTreeEvent> eventRef) {
        var event = ForgeEventFactory.blockGrowFeature(level, random, pos, optional.get());

        if (event.getResult().equals(Event.Result.DENY))
            cir.setReturnValue(false);

        eventRef.set(event);
    }

    @ModifyReceiver(method = "growMushroom", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Holder;value()Ljava/lang/Object;"))
    private Holder<?> kilt$useGrowFeatureEvent(Holder<?> instance, @Share("event") LocalRef<SaplingGrowTreeEvent> eventRef) {
        // TODO: try to make this more mod-compatible?
        return eventRef.get().getFeature();
    }
}
