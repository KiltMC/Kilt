package xyz.bluspring.kilt.injects;

import java.util.List;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import net.neoforged.fml.CrashReportCallables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.ReportType;

@Mixin(ReportType.class)
public abstract class ReportTypeInject {
    @Definition(id = "append", method = "Ljava/lang/StringBuilder;append(Ljava/lang/String;)Ljava/lang/StringBuilder;")
    @Expression("?.append('// ')")
    @Inject(method = "appendHeader", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$initCrashReportCallableHeaders(StringBuilder builder, List<String> links, CallbackInfo ci) {
        CrashReportCallables.getHeaders().forEach(header -> builder.append(header).append("\n"));
    }
}
