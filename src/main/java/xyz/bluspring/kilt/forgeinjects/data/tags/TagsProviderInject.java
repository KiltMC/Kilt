package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.TagsProvider;
import net.minecraftforge.common.extensions.IForgeTagAppender;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TagsProvider.class)
public class TagsProviderInject {

    @Mixin(TagsProvider.TagAppender.class)
    public static class TagAppenderInject<T> implements IForgeTagAppender<T> {

    }
}
