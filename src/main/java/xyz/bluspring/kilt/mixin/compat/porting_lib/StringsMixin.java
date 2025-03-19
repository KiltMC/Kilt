package xyz.bluspring.kilt.mixin.compat.porting_lib;

import joptsimple.internal.Strings;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Strings.class)
public class StringsMixin {
    @CreateStatic
    @Intrinsic
    private static String join(String[] pieces, String separator) {
        return KiltHelper.INSTANCE.joinToString(pieces, separator);
    }

    /*@CreateStatic
    @Intrinsic
    private static boolean isNullOrEmpty(String str) {
        return KiltHelper.INSTANCE.isNullOrEmpty(str);
    }*/
}