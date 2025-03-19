package xyz.bluspring.kilt.mixin.compat.porting_lib;

import joptsimple.internal.Strings;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Strings.class)
public class StringsMixin {
    /*@CreateStatic
    @Intrinsic
    private static boolean isNullOrEmpty(String str) {
        return KiltHelper.INSTANCE.isNullOrEmpty(str);
    }*/
}