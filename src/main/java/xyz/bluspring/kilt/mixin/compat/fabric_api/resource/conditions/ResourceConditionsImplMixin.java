package xyz.bluspring.kilt.mixin.compat.fabric_api.resource.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(ResourceConditionsImpl.class)
public abstract class ResourceConditionsImplMixin {
    @Definition(id = "obj", local = @Local(type = JsonObject.class, name = "obj", argsOnly = true))
    @Definition(id = "has", method = "Lcom/google/gson/JsonObject;has(Ljava/lang/String;)Z")
    @Expression("obj.has('fabric:load_conditions')")
    @WrapOperation(method = "applyResourceConditions", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$fabric_api$checkHasNeoConditions(JsonObject instance, String memberName, Operation<Boolean> original) {
        return original.call(instance, memberName) || original.call(instance, ConditionalOps.DEFAULT_CONDITIONS_KEY);
    }

    @Definition(id = "obj", local = @Local(type = JsonObject.class, name = "obj", argsOnly = true))
    @Definition(id = "get", method = "Lcom/google/gson/JsonObject;get(Ljava/lang/String;)Lcom/google/gson/JsonElement;")
    @Expression("obj.get('fabric:load_conditions')")
    @WrapOperation(method = "applyResourceConditions", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static JsonElement kilt$fabric_api$tryUseNeoConditions(JsonObject instance, String memberName, Operation<JsonElement> original) {
        if (instance.has(memberName))
            return original.call(instance, memberName);

        if (instance.has(ConditionalOps.DEFAULT_CONDITIONS_KEY))
            return original.call(instance, ConditionalOps.DEFAULT_CONDITIONS_KEY);

        return original.call(instance, memberName);
    }
}
