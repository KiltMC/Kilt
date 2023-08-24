package xyz.bluspring.kilt.injections.data.tags;

import net.minecraftforge.common.data.ExistingFileHelper;

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
