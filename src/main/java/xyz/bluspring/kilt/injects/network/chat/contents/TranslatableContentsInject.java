package xyz.bluspring.kilt.injects.network.chat.contents;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.neoforged.neoforge.common.util.InsertingContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Optional;

@Mixin(TranslatableContents.class)
public abstract class TranslatableContentsInject {
    @Shadow @Final private Object[] args;
    @Shadow @Final private String key;
    @Shadow private List<FormattedText> decomposedParts;

    @Definition(id = "decomposedWith", field = "Lnet/minecraft/network/chat/contents/TranslatableContents;decomposedWith:Lnet/minecraft/locale/Language;")
    @Definition(id = "language", local = @Local(type = Language.class))
    @Expression("this.decomposedWith = language")
    @Inject(method = "decompose", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$trySetupDecomposedParts(CallbackInfo ci, @Local Language language) {
        Component langComponent = language.getComponent(this.key);
        if (langComponent != null) {
            this.decomposedParts = ImmutableList.of(langComponent);
            ci.cancel();
        }
    }

    @Inject(method = {
        "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;",
        "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;"
    }, at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/contents/TranslatableContents;decompose()V", shift = At.Shift.AFTER), cancellable = true)
    private <T> void kilt$avoidReferenceCycle(CallbackInfoReturnable<Optional<T>> cir) {
        if (!InsertingContents.pushTranslation((TranslatableContents) (Object) this)) {
            cir.setReturnValue(Optional.empty());
        }
    }

    @WrapMethod(method = "visit(Lnet/minecraft/network/chat/FormattedText$ContentConsumer;)Ljava/util/Optional;")
    private <T> Optional<T> kilt$ensurePopTranslationIfPossible(FormattedText.ContentConsumer<T> contentConsumer, Operation<Optional<T>> original) {
        try {
            return original.call(contentConsumer);
        } finally {
            if (InsertingContents.kilt$hasTranslationInStack((TranslatableContents) (Object) this)) {
                InsertingContents.popTranslation();
            }
        }
    }

    @WrapMethod(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;")
    private <T> Optional<T> kilt$ensurePopTranslationIfPossible(FormattedText.StyledContentConsumer<T> styledContentConsumer, Style style, Operation<Optional<T>> original) {
        try {
            return original.call(styledContentConsumer, style);
        } finally {
            if (InsertingContents.kilt$hasTranslationInStack((TranslatableContents) (Object) this)) {
                InsertingContents.popTranslation();
            }
        }
    }
}
