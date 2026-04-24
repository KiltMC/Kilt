package xyz.bluspring.kilt.injects.world.level.block;

import com.google.common.collect.BiMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.neoforge.common.DataMapHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.WeatheringCopper;

@Mixin(WeatheringCopper.class)
public interface WeatheringCopperInject extends ChangeOverTimeBlock<WeatheringCopper.WeatherState> {
    @WrapOperation(method = {
        "getFirst(Lnet/minecraft/world/level/block/Block;)Lnet/minecraft/world/level/block/Block;",
        "getPrevious(Lnet/minecraft/world/level/block/Block;)Ljava/util/Optional;"
    }, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/BiMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T, U> U kilt$tryGetPreviousOxidizedStage(BiMap<T, U> instance, T o, Operation<U> original) {
        if (o instanceof Block block) {
            var value = DataMapHooks.getPreviousOxidizedStage(block);

            if (value != null) {
                return (U) value;
            }
        }

        return original.call(instance, o);
    }

    @WrapOperation(method = {
        "getNext(Lnet/minecraft/world/level/block/Block;)Ljava/util/Optional;"
    }, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/BiMap;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private static <T, U> U kilt$tryGetNextOxidizedStage(BiMap<T, U> instance, T o, Operation<U> original) {
        if (o instanceof Block block) {
            var value = DataMapHooks.getNextOxidizedStage(block);

            if (value != null) {
                return (U) value;
            }
        }

        return original.call(instance, o);
    }
}
