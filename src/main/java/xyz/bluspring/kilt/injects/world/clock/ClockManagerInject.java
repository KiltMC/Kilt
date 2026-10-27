package xyz.bluspring.kilt.injects.world.clock;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.clock.ClockManagerInjection;

import net.minecraft.core.Holder;
import net.minecraft.world.clock.ClockManager;
import net.minecraft.world.clock.WorldClock;

@Mixin(ClockManager.class)
public interface ClockManagerInject extends ClockManagerInjection {
    @Override
    default float getPartialTick(Holder<WorldClock> definition) {
        return 0;
    }

    @Override
    default void setRate(Holder<WorldClock> definition, float rate) {
    }
}
