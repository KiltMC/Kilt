package xyz.bluspring.kilt.injects.util.context;

import java.util.Set;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.util.context.ContextMapInjection;

import net.minecraft.util.context.ContextMap;

@Mixin(ContextMap.class)
public abstract class ContextMapInject implements ContextMapInjection {
    @CreateStatic
    private static final ContextMap EMPTY = ContextMapInjection.EMPTY;

    @Mixin(ContextMap.Builder.class)
    public abstract static class BuilderInject {
        @Definition(id = "notAllowed", local = @Local(type = Set.class, name = "notAllowed"))
        @Definition(id = "isEmpty", method = "Ljava/util/Set;isEmpty()Z")
        @Expression("notAllowed.isEmpty()")
        @ModifyExpressionValue(method = "create", at = @At("MIXINEXTRAS:EXPRESSION"))
        private boolean kilt$noopParamSetValidation(boolean original) {
            return true;
        }
    }
}
