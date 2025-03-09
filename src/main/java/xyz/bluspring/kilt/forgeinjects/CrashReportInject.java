package xyz.bluspring.kilt.forgeinjects;

import net.minecraft.CrashReport;
import net.minecraft.SystemReport;
import net.minecraftforge.logging.CrashReportExtender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CrashReport.class)
public abstract class CrashReportInject {
    @Shadow @Final private Throwable exception;

    @Shadow private StackTraceElement[] uncategorizedStackTrace;

    @Shadow @Final private SystemReport systemReport;

    @Inject(method = "getDetails(Ljava/lang/StringBuilder;)V", at = @At(value = "INVOKE", target = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;", ordinal = 3, shift = At.Shift.BEFORE))
    private void kilt$appendSuspectedMods(StringBuilder builder, CallbackInfo ci) {
        //builder.append(CrashReportAnalyser.appendSuspectedMods(this.exception, this.uncategorizedStackTrace));
    }

    @Inject(method = "getDetails(Ljava/lang/StringBuilder;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/SystemReport;appendToCrashReportString(Ljava/lang/StringBuilder;)V", shift = At.Shift.BEFORE))
    private void kilt$extendSystemReport(StringBuilder builder, CallbackInfo ci) {
        CrashReportExtender.extendSystemReport(this.systemReport);
    }

}
