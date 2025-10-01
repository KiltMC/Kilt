// TRACKED HASH: b4feb521213eda395cfa40562ffc85cb3f3608e9
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
import xyz.bluspring.kilt.injections.world.level.LevelSettingsInjection;
import xyz.bluspring.kilt.injections.world.level.storage.PrimaryLevelDataInjection;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataInject implements PrimaryLevelDataInjection, ServerLevelData {
    @Shadow private LevelSettings settings;
    @Unique private boolean confirmedExperimentalWarning = false;

    @SuppressWarnings("InvalidInjectorMethodSignature") // idk what it's on
    @ModifyReturnValue(method = "parse", at = @At("RETURN"))
    private static <T> PrimaryLevelData kilt$loadNeoForgeLevelData(PrimaryLevelData original, @Local(argsOnly = true) Lifecycle lifecycle, @Local(argsOnly = true) Dynamic<T> dynamic) {
        var result = original
                .withConfirmedWarning(lifecycle != Lifecycle.stable() && dynamic.get("confirmedExperimentalSettings").asBoolean(false));

        result.setDayTimeFraction(dynamic.get("neoDayTimeFraction").asFloat(0f));
        result.setDayTimePerTick(dynamic.get("neoDayTimePerTick").asFloat(-1f));

        return result;
    }

    @Inject(method = "setTagData", at = @At("TAIL"))
    private void kilt$addForgeLevelData(RegistryAccess registry, CompoundTag nbt, CompoundTag playerNBT, CallbackInfo ci) {
        nbt.putString("forgeLifecycle", CommonHooks.encodeLifecycle(this.settings.getLifecycle()));
        nbt.putBoolean("confirmedExperimentalSettings", this.confirmedExperimentalWarning);
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

    // Variable day time code :,D
    private float dayTimeFraction = 0.0f;
    private float dayTimePerTick = -1.0f;

    @Override
    public void setDayTimeFraction(float dayTimeFraction) {
        this.dayTimeFraction = dayTimeFraction;
    }

    @Override
    public float getDayTimeFraction() {
        return dayTimeFraction;
    }

    @Override
    public float getDayTimePerTick() {
        return dayTimePerTick;
    }

    @Override
    public void setDayTimePerTick(float dayTimePerTick) {
        this.dayTimePerTick = dayTimePerTick;
    }
}