package xyz.bluspring.kilt.injects.world.level.storage;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.storage.PrimaryLevelDataInjection;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataInject implements PrimaryLevelDataInjection, ServerLevelData {
    @Shadow
    private LevelSettings settings;
    @Unique private boolean confirmedExperimentalWarning = false;

    @ModifyReturnValue(method = "parse", at = @At("RETURN"))
    private static <T> PrimaryLevelData kilt$appendAdditionalLevelData(PrimaryLevelData original, @Local(argsOnly = true) Dynamic<T> tag, @Local(argsOnly = true) Lifecycle lifecycle) {
        var result = original
            .withConfirmedWarning(lifecycle != Lifecycle.stable() && tag.get("confirmedExperimentalSettings").asBoolean(false));

        result.setDayTimeFraction(tag.get("neoDayTimeFraction").asFloat(0f));
        result.setDayTimePerTick(tag.get("neoDayTimePerTick").asFloat(-1f));

        return result;
    }

    @Inject(method = "setTagData", at = @At("TAIL"))
    private void kilt$saveCustomNeoData(RegistryAccess registry, CompoundTag nbt, CompoundTag playerNBT, CallbackInfo ci) {
        nbt.putString("forgeLifecycle", CommonHooks.encodeLifecycle(this.settings.getLifecycle()));
        nbt.putBoolean("confirmedExperimentalSettings", this.confirmedExperimentalWarning);
        nbt.putFloat("neoDayTimeFraction", this.dayTimeFraction);
        nbt.putFloat("neoDayTimePerTick", this.dayTimePerTick);
    }

    @Override
    public boolean hasConfirmedExperimentalWarning() {
        return this.confirmedExperimentalWarning;
    }

    @Override
    public PrimaryLevelData withConfirmedWarning(boolean confirmedWarning) {
        this.confirmedExperimentalWarning = confirmedWarning;
        return (PrimaryLevelData) (Object) this;
    }

    @Unique private float dayTimeFraction = 0f;
    @Unique private float dayTimePerTick = -1f;

    @Override
    public float getDayTimeFraction() {
        return this.dayTimeFraction;
    }

    @Override
    public float getDayTimePerTick() {
        return this.dayTimePerTick;
    }

    @Override
    public void setDayTimeFraction(float dayTimeFraction) {
        this.dayTimeFraction = dayTimeFraction;
    }

    @Override
    public void setDayTimePerTick(float dayTimePerTick) {
        this.dayTimePerTick = dayTimePerTick;
    }
}
