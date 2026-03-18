package xyz.bluspring.kilt.injects.stats;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.player.Player;

@Mixin(StatsCounter.class)
public abstract class StatsCounterInject {
    @WrapOperation(method = "setValue", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntMap;put(Ljava/lang/Object;I)I"))
    private <V> int kilt$handleStatAwardEvent(Object2IntMap<V> instance, V o, int i, Operation<Integer> original, @Local(argsOnly = true) Player player) {
        if (o instanceof Stat<?> stat) {
            var event = EventHooks.onStatAward(player, stat, i);
            if (!event.isCanceled()) {
                return original.call(instance, event.getStat(), event.getValue());
            } else {
                return 0;
            }
        }

        return original.call(instance, o, i);
    }
}
