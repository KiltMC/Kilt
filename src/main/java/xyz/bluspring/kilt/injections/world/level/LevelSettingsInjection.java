package xyz.bluspring.kilt.injections.world.level;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.LevelSettings;

public interface LevelSettingsInjection {
    LevelSettings withLifecycle(Lifecycle lifecycle);
    Lifecycle getLifecycle();
    void kilt$setLifecycle(Lifecycle lifecycle);
}