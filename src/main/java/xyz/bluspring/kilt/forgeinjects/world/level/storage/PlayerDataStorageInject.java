// TRACKED HASH: 361fb741749535140b255a8b9d4bd2ac5e288ca8
package xyz.bluspring.kilt.forgeinjects.world.level.storage;

import net.minecraft.world.level.storage.PlayerDataStorage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.PlayerDataStorageInjection;

import java.io.File;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageInject implements PlayerDataStorageInjection {
    @Shadow @Final private File playerDir;

    @NotNull
    @Override
    public File getPlayerDataFolder() {
        return this.playerDir;
    }
}