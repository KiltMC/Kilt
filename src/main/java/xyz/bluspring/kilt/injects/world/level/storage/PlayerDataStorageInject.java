package xyz.bluspring.kilt.injects.world.level.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.storage.PlayerDataStorageInjection;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public abstract class PlayerDataStorageInject implements PlayerDataStorageInjection {
    @Shadow @Final private File playerDir;

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/nio/file/Path;Ljava/nio/file/Path;Ljava/nio/file/Path;)V", shift = At.Shift.AFTER))
    private void kilt$handlePlayerSaveEvent(Player player, CallbackInfo ci) {
        EventHooks.firePlayerSavingEvent(player, this.playerDir, player.getStringUUID());
    }

    @Inject(method = "method_55788", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;load(Lnet/minecraft/nbt/CompoundTag;)V", shift = At.Shift.AFTER))
    private void kilt$handlePlayerLoadEvent(Player player, CompoundTag compoundTag, CallbackInfoReturnable<CompoundTag> cir) {
        EventHooks.firePlayerLoadingEvent(player, this.playerDir, player.getStringUUID());
    }

    @Override
    public File getPlayerDir() {
        return this.playerDir;
    }
}
