package xyz.bluspring.kilt.injects.util;

import com.google.common.collect.BiMap;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.util.ExtraCodecsInjection;

import net.minecraft.util.ExtraCodecs;

@Mixin(ExtraCodecs.class)
public abstract class ExtraCodecsInject {
    @Mixin(ExtraCodecs.LateBoundIdMapper.class)
    public abstract static class LateBoundIdMapperInject<I, V> implements ExtraCodecsInjection.LateBoundIdMapperInjection<I, V> {
        @Shadow @Final private BiMap<I, V> idToValue;

        @Override
        public @Nullable V getValue(I id) {
            return this.idToValue.get(id);
        }
    }
}
