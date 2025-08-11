package xyz.bluspring.kilt.injections.blaze3d.systems;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.neoforged.neoforge.client.GlStateBackup;
import xyz.bluspring.kilt.injections.blaze3d.platform.GlStateManagerInjection;

public interface RenderSystemInjection {
    static void backupGlState(GlStateBackup state) {
        RenderSystem.assertOnRenderThread();
        GlStateManagerInjection._backupGlState(state);
    }

    static void restoreGlState(GlStateBackup state) {
        RenderSystem.assertOnRenderThread();
        GlStateManagerInjection._restoreGlState(state);
    }
}
