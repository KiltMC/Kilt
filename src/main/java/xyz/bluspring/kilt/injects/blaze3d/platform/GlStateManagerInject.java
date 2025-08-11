// TRACKED HASH: dfe2cf4afa6f2c9ef91cc552ca83f15c1b525aed
package xyz.bluspring.kilt.injects.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import net.neoforged.neoforge.client.GlStateBackup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.blaze3d.platform.GlStateManagerInjection;

@Mixin(value = GlStateManager.class, remap = false)
public abstract class GlStateManagerInject {
    @Unique
    @CreateStatic
    private static void _backupGlState(GlStateBackup state) {
        GlStateManagerInjection._backupGlState(state);
    }

    @Unique
    @CreateStatic
    private static void _restoreGlState(GlStateBackup state) {
        GlStateManagerInjection._restoreGlState(state);
    }
}