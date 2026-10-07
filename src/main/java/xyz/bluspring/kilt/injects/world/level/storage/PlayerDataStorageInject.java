package xyz.bluspring.kilt.injects.world.level.storage;

import java.io.File;

import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.storage.PlayerDataStorageInjection;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Mixin(PlayerDataStorage.class)
public abstract class PlayerDataStorageInject implements PlayerDataStorageInjection {
    @Shadow @Final private File playerDir;

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;safeReplaceFile(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V", shift = At.Shift.AFTER))
    private void kilt$handlePlayerSaveEvent(Player player, CallbackInfo ci) {
        EventHooks.firePlayerSavingEvent(player, this.playerDir, player.getStringUUID());
    }

    @Override
    public File getPlayerDir() {
        return this.playerDir;
    }
}
