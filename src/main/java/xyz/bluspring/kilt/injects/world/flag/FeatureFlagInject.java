package xyz.bluspring.kilt.injects.world.flag;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagUniverse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.flag.FeatureFlagInjection;

@Mixin(FeatureFlag.class)
public abstract class FeatureFlagInject implements FeatureFlagInjection {
    @Unique int extMaskIndex = 0;
    @Unique boolean modded = false;

    FeatureFlagInject(FeatureFlagUniverse universe, int mask) {}

    @CreateInitializer
    FeatureFlagInject(FeatureFlagUniverse universe, int mask, int offset, boolean modded) {
        this(universe, mask);
        this.extMaskIndex = offset - 1;
        this.modded = modded;
    }

    @Override
    public boolean isModded() {
        return modded;
    }

    @Override
    public void kilt$setModded(boolean modded) {
        this.modded = modded;
    }

    @Override
    public int kilt$extMaskIndex() {
        return this.extMaskIndex;
    }

    @Override
    public void kilt$setExtMaskIndex(int index) {
        this.extMaskIndex = index;
    }
}
