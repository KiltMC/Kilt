package xyz.bluspring.kilt.injects.client.gui.components;

import java.util.function.Predicate;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.client.gui.components.EditBoxInjection;

import net.minecraft.client.gui.components.EditBox;

@Mixin(EditBox.class)
public abstract class EditBoxInject implements EditBoxInjection {
    @Shadow
    private boolean textShadow;
    @Unique private Predicate<String> filter = _ -> true;

    @Inject(method = "setValue", at = @At("HEAD"), cancellable = true)
    private void kilt$testFilter(String value, CallbackInfo ci) {
        if (!this.filter.test(value))
            ci.cancel();
    }

    @Override
    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    @Definition(id = "value", field = "Lnet/minecraft/client/gui/components/EditBox;value:Ljava/lang/String;")
    @Definition(id = "StringBuilder", type = StringBuilder.class)
    @Definition(id = "replace", method = "Ljava/lang/StringBuilder;replace(IILjava/lang/String;)Ljava/lang/StringBuilder;")
    @Definition(id = "toString", method = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;")
    @Expression("this.value = new StringBuilder(?).replace(?, ?, ?).toString()")
    @Inject(method = "insertText", at = @At("MIXINEXTRAS:EXPRESSION"), cancellable = true)
    private void kilt$filterInputInText(String input, CallbackInfo ci, @Local(name = "text") String text) {
        if (!this.filter.test(text))
            ci.cancel();
    }

    @Override
    @Accessor
    public abstract boolean getTextShadow();
}
