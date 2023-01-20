package xyz.bluspring.kilt.mixin;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TagsProvider.class)
public interface TagsProviderAccessor<T> {
    @Invoker
    TagBuilder callGetOrCreateRawBuilder(TagKey<T> tagKey);
}
