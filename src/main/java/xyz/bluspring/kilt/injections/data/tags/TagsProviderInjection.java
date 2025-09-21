package xyz.bluspring.kilt.injections.data.tags;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.TagBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;

@FabricInjectedInterface(TagsProvider.class)
public interface TagsProviderInjection {
    default void kilt$setModId(String modId) {
        throw new IllegalStateException();
    }

    default void kilt$setExistingFileHelper(ExistingFileHelper fileHelper) {
        throw new IllegalStateException();
    }

    default void kilt$addConstructorArgs(String modId, ExistingFileHelper fileHelper) {
        this.kilt$setModId(modId);
        this.kilt$setExistingFileHelper(fileHelper);
    }
}
