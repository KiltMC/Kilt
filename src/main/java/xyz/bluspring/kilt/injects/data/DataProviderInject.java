package xyz.bluspring.kilt.injects.data;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.data.DataProviderInjection;

import net.minecraft.data.DataProvider;

@Mixin(DataProvider.class)
public interface DataProviderInject {
    @Inject(method = "method_43808", at = @At("HEAD"))
    private static void kilt$addNeoConditionsOrder(Object2IntOpenHashMap<String> map, CallbackInfo ci) {
        map.put("neoforge:conditions", -1);
    }

    @Expression("'  '")
    @ModifyExpressionValue(method = "method_46567", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static String kilt$tryUseNeoRepeatWidth(String original) {
        if (DataProviderInjection.INDENT_WIDTH.get() == 2)
            return original;

        return " ".repeat(Math.max(0, DataProviderInjection.INDENT_WIDTH.get()));
    }
}
