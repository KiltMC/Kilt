// TRACKED HASH: 361fb741749535140b255a8b9d4bd2ac5e288ca8
package xyz.bluspring.kilt.forgeinjects.world.level.storage;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.PlayerDataStorageInjection;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageInject implements PlayerDataStorageInjection {
    @Shadow @Final private File playerDir;

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;safeReplaceFile(Ljava/io/File;Ljava/io/File;Ljava/io/File;)V", shift = At.Shift.AFTER))
    private void kilt$onPlayerSavingEvent(Player player, CallbackInfo ci) {
        ForgeEventFactory.firePlayerSavingEvent(player, playerDir, player.getStringUUID());
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void kilt$onPlayerLoadingEvent(Player player, CallbackInfoReturnable<CompoundTag> cir) {
        ForgeEventFactory.firePlayerLoadingEvent(player, playerDir, player.getStringUUID());
    }

    @NotNull
    @Override
    public File getPlayerDataFolder() {
        return this.playerDir;
    }
}