package xyz.bluspring.kilt.forgeinjects.world.level.storage;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.LevelSettingsInjection;
import xyz.bluspring.kilt.injections.world.level.storage.PrimaryLevelDataInjection;

@Mixin(PrimaryLevelData.class)
public abstract class PrimaryLevelDataInject implements PrimaryLevelDataInjection {
    @Shadow private LevelSettings settings;
    @Unique private boolean confirmedExperimentalWarning = false;

    @ModifyReturnValue(method = "parse", at = @At("RETURN"))
    private static PrimaryLevelData kilt$loadForgeLevelData(PrimaryLevelData original, @Local(argsOnly = true) Lifecycle lifecycle, @Local(argsOnly = true) Dynamic<?> dynamic) {
        return ((PrimaryLevelDataInjection) original).withConfirmedWarning(lifecycle != Lifecycle.stable() && dynamic.get("confirmedExperimentalSettings").asBoolean(false));
    }

    @Inject(method = "setTagData", at = @At("TAIL"))
    private void kilt$addForgeLevelData(RegistryAccess registry, CompoundTag nbt, CompoundTag playerNBT, CallbackInfo ci) {
        nbt.putString("forgeLifecycle", ForgeHooks.encodeLifecycle(((LevelSettingsInjection) (Object) this.settings).getLifecycle()));
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
}