package xyz.bluspring.kilt.forgeinjects;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.CrashReportCategory;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.CrashReportCategoryInjection;
import xyz.bluspring.kilt.remaps.client.renderer.ScreenEffectRendererRemap;

@Mixin(CrashReportCategory.class)
public class CrashReportCategoryInject implements CrashReportCategoryInjection {
    @Shadow private StackTraceElement[] stackTrace;

    @Override
    public void applyStackTrace(Throwable trace) {
        this.stackTrace = trace.getStackTrace();
    }
}
