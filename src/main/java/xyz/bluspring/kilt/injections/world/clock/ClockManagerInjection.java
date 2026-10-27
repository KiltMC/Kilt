package xyz.bluspring.kilt.injections.world.clock;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;

public interface ClockManagerInjection {
    default float getPartialTick(Holder<WorldClock> definition) {
        throw KiltHelper.createMixinException(ClockManagerInjection.class, "getPartialTick");
    }

    default void setRate(Holder<WorldClock> definition, float rate) {
        throw KiltHelper.createMixinException(ClockManagerInjection.class, "setRate");
    }
}
