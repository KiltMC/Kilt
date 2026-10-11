package xyz.bluspring.kilt.injections.util;

import org.jspecify.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

public interface ExtraCodecsInjection {
    interface LateBoundIdMapperInjection<I, V> {
        default @Nullable V getValue(I id) {
            throw KiltHelper.createMixinException(LateBoundIdMapperInjection.class, "getValue");
        }
    }
}
