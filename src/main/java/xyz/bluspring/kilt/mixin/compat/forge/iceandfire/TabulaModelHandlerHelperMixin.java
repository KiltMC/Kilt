package xyz.bluspring.kilt.mixin.compat.forge.iceandfire;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import kotlin.text.StringsKt;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStream;
import java.util.Locale;

@Pseudo
@Mixin(targets = "com.github.alexthe666.iceandfire.client.model.util.TabulaModelHandlerHelper")
public class TabulaModelHandlerHelperMixin {
    @Dynamic
    @WrapOperation(method = "loadTabulaModel", at = @At(value = "INVOKE", target = "Ljava/lang/ClassLoader;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"))
    private static InputStream kilt$tryGetResourceFromKnot(ClassLoader instance, String string, Operation<InputStream> original) {
        var value = original.call(instance, string);

        if (value == null) {
            // Kilt: This is done due to an inconsistency between Knot and ModLauncher, where "/path/to" doesn't point to the mod resources unless you ran it from the .class,
            //       and ModLauncher also seems to be case-insensitive with this.
            return original.call(instance, StringsKt.removePrefix(string, "/").toLowerCase(Locale.ENGLISH));
        }

        return value;
    }
}
