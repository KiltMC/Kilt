package xyz.bluspring.kilt.forgeinjects;

import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
