package xyz.bluspring.kilt.forgeinjects.core;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.neoforged.neoforge.common.extensions.IHolderExtension;
import net.neoforged.neoforge.registries.IRegistryExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Registry.class)
public interface RegistryInject<T> extends IRegistryExtension<T> {
    @Definition(id = "value", local = @Local(type = Holder.class, argsOnly = true))
    @Definition(id = "Reference", type = Holder.Reference.class)
    @Expression("@(value) instanceof Reference")
    @ModifyExpressionValue(method = "safeCastToReference", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Holder<T> kilt$tryUseForgeDelegate(Holder<T> original) {
        var delegate = ((IHolderExtension<T>) original).getDelegate();

        //noinspection RedundantIfStatement
        if (original == delegate)
            return original;

        return delegate;
    }
}
