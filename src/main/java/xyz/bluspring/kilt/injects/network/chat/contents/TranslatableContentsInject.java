package xyz.bluspring.kilt.injects.network.chat.contents;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.internal.TextComponentMessageFormatHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsInject {
    @Shadow @Final private Object[] args;

    @Definition(id = "formatTemplate", local = @Local(type = String.class, argsOnly = true))
    @Definition(id = "length", method = "Ljava/lang/String;length()I")
    @Expression("? < formatTemplate.length()")
    @Inject(method = "decomposeTemplate", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$handleForgeI18n(String formatTemplate, Consumer<FormattedText> consumer, CallbackInfo ci, @Local(ordinal = 1) LocalIntRef j) {
        if (j.get() == 0) {
            j.set(TextComponentMessageFormatHandler.handle((TranslatableContents) (Object) this, consumer, this.args, formatTemplate));
        }
    }
}
