package xyz.bluspring.kilt.injections.world.clock;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.Holder;
import net.minecraft.world.clock.WorldClock;

public interface ServerClockManagerInjection {
    default float getRate(Holder<WorldClock> definition) {
        throw KiltHelper.createMixinException(ServerClockManagerInjection.class, "getRate");
    }

    default float getPartialTick(Holder<WorldClock> definition) {
        throw KiltHelper.createMixinException(ServerClockManagerInjection.class, "getPartialTick");
    }

    interface ClockInstanceInjection {
        default Holder<WorldClock> kilt$getHolder() {
            throw KiltHelper.createMixinException(ClockInstanceInjection.class, "kilt$getHolder");
        }

        default void kilt$setHolder(Holder<WorldClock> definition) {
            throw KiltHelper.createMixinException(ClockInstanceInjection.class, "kilt$setHolder");
        }

        default void tick(boolean advanceTime) {
            throw KiltHelper.createMixinException(ClockInstanceInjection.class, "tick");
        }

        default float kilt$getRate() {
            throw KiltHelper.createMixinException(ClockInstanceInjection.class, "kilt$getRate");
        }

        default float kilt$getPartialTick() {
            throw KiltHelper.createMixinException(ClockInstanceInjection.class, "kilt$getPartialTick");
        }
    }
}
