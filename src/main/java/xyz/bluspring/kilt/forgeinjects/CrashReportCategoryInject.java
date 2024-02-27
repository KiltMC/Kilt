// TRACKED HASH: c9ae7f6c9276aa62194bc83803132dfe3cad1b05
package xyz.bluspring.kilt.forgeinjects;

import net.minecraft.CrashReportCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.CrashReportCategoryInjection;

@Mixin(CrashReportCategory.class)
public class CrashReportCategoryInject implements CrashReportCategoryInjection {
    @Shadow private StackTraceElement[] stackTrace;

    @Override
    public void applyStackTrace(Throwable trace) {
        this.stackTrace = trace.getStackTrace();
    }
}