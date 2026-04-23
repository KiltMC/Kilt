package xyz.bluspring.kilt.injects.world.flag;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.flag.FeatureFlagInjection;

import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagUniverse;

@Mixin(FeatureFlag.class)
public abstract class FeatureFlagInject implements FeatureFlagInjection {
    @Shadow @Final private FeatureFlagUniverse universe;
    @Shadow @Final private long mask;

    @Unique int extMaskIndex = -1;
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

    @Override
    public FeatureFlagUniverse kilt$universe() {
        return this.universe;
    }

    @Override
    public long kilt$mask() {
        return this.mask;
    }
}
