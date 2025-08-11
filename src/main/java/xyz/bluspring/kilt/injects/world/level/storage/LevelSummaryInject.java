// TRACKED HASH: 49d3e98d447954431e252c39c4d4cbe1b6656415
package xyz.bluspring.kilt.injects.world.level.storage;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelSummary;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.LevelSettingsInjection;
import xyz.bluspring.kilt.injections.world.level.storage.LevelSummaryInjection;

@Mixin(LevelSummary.class)
public abstract class LevelSummaryInject implements LevelSummaryInjection {
    @Shadow @Final private LevelSettings settings;

    @Override
    public boolean isLifecycleExperimental() {
        return ((LevelSettingsInjection) (Object) this.settings).getLifecycle().equals(Lifecycle.experimental());
    }
}