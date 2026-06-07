package xyz.bluspring.kilt.injects.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DataResult;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.registries.IRegistryExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;

@Mixin(Registry.class)
public interface RegistryInject<T> extends IRegistryExtension<T> {
    @Definition(id = "value", local = @Local(type = Holder.class, argsOnly = true))
    @Definition(id = "Reference", type = Holder.Reference.class)
    @Expression("@(value) instanceof Reference")
    @ModifyExpressionValue(method = "safeCastToReference", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Holder<T> kilt$tryUseForgeDelegate(Holder<T> original, @Cancellable CallbackInfoReturnable<DataResult<Holder.Reference<T>>> cir) {
        var delegate = ((IHolderExtension<T>) original).getDelegate();

        //noinspection RedundantIfStatement
        if (original == delegate)
            return original;

        if (delegate instanceof Holder.Reference<T> reference) {
            cir.setReturnValue(DataResult.success(reference));
            return reference;
        }

        return original;
    }
}
