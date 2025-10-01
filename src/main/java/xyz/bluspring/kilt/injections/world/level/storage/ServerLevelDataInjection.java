package xyz.bluspring.kilt.injections.world.level.storage;

import xyz.bluspring.kilt.util.KiltHelper;

public interface ServerLevelDataInjection {
    default void setDayTimeFraction(float dayTimeFraction) {
        throw KiltHelper.createMixinException(ServerLevelDataInjection.class, "setDayTimeFraction");
    }

    default float getDayTimeFraction() {
        throw KiltHelper.createMixinException(ServerLevelDataInjection.class, "getDayTimeFraction");
    }

    default float getDayTimePerTick() {
        throw KiltHelper.createMixinException(ServerLevelDataInjection.class, "getDayTimePerTick");
    }

    default void setDayTimePerTick(float dayTimePerTick) {
        throw KiltHelper.createMixinException(ServerLevelDataInjection.class, "setDayTimePerTick");
    }
}
