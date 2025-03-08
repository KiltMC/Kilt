package xyz.bluspring.kilt.forgeinjects.client.gui.screens.worldselection;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.storage.LevelStorageAccessInjection;
import xyz.bluspring.kilt.injections.world.level.storage.PrimaryLevelDataInjection;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsInject {
    @Unique private boolean kilt$shouldConfirmExperimentalWarning = false;
    @Shadow protected abstract void doLoadLevel(Screen lastScreen, String levelName, boolean safeMode, boolean checkAskForBackup);

    private void doLoadLevel(Screen lastScreen, String levelName, boolean safeMode, boolean checkAskForBackup, boolean confirmExperimentalWarning) {
        this.kilt$shouldConfirmExperimentalWarning = confirmExperimentalWarning;
        this.doLoadLevel(lastScreen, levelName, safeMode, checkAskForBackup);
    }

    @Inject(method = "doLoadLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;loadWorldStem(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;ZLnet/minecraft/server/packs/repository/PackRepository;)Lnet/minecraft/server/WorldStem;"))
    private void kilt$loadModdedWorldData(Screen lastScreen, String levelName, boolean safeMode, boolean checkAskForBackup, CallbackInfo ci, @Local LevelStorageSource.LevelStorageAccess access) {
        ((LevelStorageAccessInjection) access).readAdditionalLevelSaveData();
    }

    @Inject(method = "doLoadLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;loadWorldStem(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;ZLnet/minecraft/server/packs/repository/PackRepository;)Lnet/minecraft/server/WorldStem;", shift = At.Shift.BY, by = 2))
    private void kilt$setConfirmWarning(Screen lastScreen, String levelName, boolean safeMode, boolean checkAskForBackup, CallbackInfo ci, @Local WorldStem worldStem) {
        if (kilt$shouldConfirmExperimentalWarning && worldStem.worldData() instanceof PrimaryLevelData pld) {
            ((PrimaryLevelDataInjection) pld).withConfirmedWarning(true);
            kilt$shouldConfirmExperimentalWarning = false;
        }
    }

    // TODO: figure out how to skip confirmation
}