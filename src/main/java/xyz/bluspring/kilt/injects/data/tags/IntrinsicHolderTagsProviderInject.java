package xyz.bluspring.kilt.injects.data.tags;

import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.extensions.IForgeIntrinsicHolderTagAppender;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Function;

@Mixin(IntrinsicHolderTagsProvider.class)
public abstract class IntrinsicHolderTagsProviderInject {
    // Kilt: we have no reason to implement this

    @Mixin(IntrinsicHolderTagsProvider.IntrinsicTagAppender.class)
    public abstract static class IntrinsicTagAppenderInject<T> implements IForgeIntrinsicHolderTagAppender<T> {
        @Shadow @Final private Function<T, ResourceKey<T>> keyExtractor;

        @Override
        public ResourceKey<T> getKey(T value) {
            return this.keyExtractor.apply(value);
        }
    }
}
