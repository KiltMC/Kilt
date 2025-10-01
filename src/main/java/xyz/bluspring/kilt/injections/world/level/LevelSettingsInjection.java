package xyz.bluspring.kilt.injections.world.level;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.LevelSettings;
import xyz.bluspring.kilt.util.KiltHelper;

public interface LevelSettingsInjection {
    default LevelSettings withLifecycle(Lifecycle lifecycle) {
        throw KiltHelper.createMixinException(LevelSettingsInjection.class, "withLifecycle");
    }

    default Lifecycle getLifecycle() {
        throw KiltHelper.createMixinException(LevelSettingsInjection.class, "getLifecycle");
    }

    default void kilt$setLifecycle(Lifecycle lifecycle) {
        throw KiltHelper.createMixinException(LevelSettingsInjection.class, "kilt$setLifecycle");
    }
}
