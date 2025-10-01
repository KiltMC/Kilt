package xyz.bluspring.kilt.injects.world.level.storage;

import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(DerivedLevelData.class)
public abstract class DerivedLevelDataInject implements ServerLevelData {
    @Shadow
    @Final
    private ServerLevelData wrapped;

    @Override
    public void setDayTimeFraction(float dayTimeFraction) {
        this.wrapped.setDayTimeFraction(dayTimeFraction);
    }

    @Override
    public float getDayTimeFraction() {
        return this.wrapped.getDayTimeFraction();
    }

    @Override
    public float getDayTimePerTick() {
        return this.wrapped.getDayTimePerTick();
    }

    @Override
    public void setDayTimePerTick(float dayTimePerTick) {
        this.wrapped.setDayTimePerTick(dayTimePerTick);
    }
}
