package xyz.bluspring.kilt.injects.util.datafix.fixes;

import java.util.Map;

import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Dynamic;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.util.datafix.fixes.StructuresBecomeConfiguredFix;

@Mixin(StructuresBecomeConfiguredFix.class)
public abstract class StructuresBecomeConfiguredFixInject {
    @WrapOperation(method = "findUpdatedStructureType", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0))
    private <K, V> V kilt$getForgeStructureConversion(Map<K, V> instance, Object o, Operation<V> original) {
        var value = original.call(instance, o);
        if (value == null) {
            return (V) CommonHooks.getStructureConversion((String) o);
        }

        return value;
    }

    @Expression("return null")
    @ModifyReturnValue(method = "findUpdatedStructureType", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Dynamic<?> kilt$tryPassThroughStructureIds(Dynamic<?> original, @Local String s, @Local(argsOnly = true, ordinal = 1) Dynamic<?> dyn2) {
        if (original != null)
            return original;

        if (CommonHooks.checkStructureNamespace(s))
            return dyn2.createString(s);

        return dyn2.createString("unknown." + s);
    }
}
