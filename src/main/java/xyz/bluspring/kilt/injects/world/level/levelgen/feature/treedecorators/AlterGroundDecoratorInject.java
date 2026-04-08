package xyz.bluspring.kilt.injects.world.level.levelgen.feature.treedecorators;

import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.level.AlterGroundEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.treedecorators.AlterGroundDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;

@Mixin(AlterGroundDecorator.class)
public abstract class AlterGroundDecoratorInject {
    @Shadow @Final private BlockStateProvider provider;
    @Unique private final ThreadLocal<AlterGroundEvent.StateProvider> kilt$eventProvider = new ThreadLocal<>();

    @Definition(id = "get", method = "Ljava/util/List;get(I)Ljava/lang/Object;")
    @Definition(id = "list", local = @Local(type = List.class, ordinal = 0))
    @Definition(id = "BlockPos", type = BlockPos.class)
    @Definition(id = "getY", method = "Lnet/minecraft/core/BlockPos;getY()I")
    @Expression("((BlockPos) list.get(0)).getY()")
    @Inject(method = "place", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$callAlterGroundEvent(TreeDecorator.Context context, CallbackInfo ci, @Share("eventProvider") LocalRef<AlterGroundEvent.StateProvider> eventProvider, @Local List<BlockPos> list) {
        AlterGroundEvent.StateProvider provider = this.provider::getState;
        eventProvider.set(EventHooks.alterGround(context, list, provider));

        if (eventProvider.get() != provider)
            this.kilt$eventProvider.set(eventProvider.get());
    }

    @Inject(method = "place", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
    private void kilt$clearEventProvider(TreeDecorator.Context context, CallbackInfo ci) {
        this.kilt$eventProvider.remove();
    }

    @WrapOperation(method = "placeBlockAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/levelgen/feature/stateproviders/BlockStateProvider;getState(Lnet/minecraft/util/RandomSource;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"))
    private BlockState kilt$tryUseCustomEventProvider(BlockStateProvider instance, RandomSource randomSource, BlockPos pos, Operation<BlockState> original) {
        var eventProvider = this.kilt$eventProvider.get();
        if (eventProvider != null) {
            return eventProvider.getState(randomSource, pos);
        }

        return original.call(instance, randomSource, pos);
    }
}
