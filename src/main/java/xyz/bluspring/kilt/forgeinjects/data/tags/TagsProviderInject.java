package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.extensions.IForgeTagAppender;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.data.tags.TagsProviderInjection;

import java.nio.file.Path;

@Mixin(TagsProvider.class)
public abstract class TagsProviderInject<T> implements TagsProviderInjection {
    @Shadow @Final
    protected ResourceKey<? extends Registry<T>> registryKey;
    @Shadow @Final
    protected PackOutput.PathProvider pathProvider;

    protected String modId = "vanilla";
    protected ExistingFileHelper existingFileHelper = null;
    @Unique private final ExistingFileHelper.IResourceType resourceType = new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", TagManager.getTagDir(this.registryKey));
    @Unique private final ExistingFileHelper.IResourceType elementResourceType = new ExistingFileHelper.ResourceType(PackType.SERVER_DATA, ".json", ForgeHooks.prefixNamespace(this.registryKey.location()));

    @CreateInitializer
    protected TagsProviderInject(DataGenerator dataGenerator, Registry<T> registry, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        this(dataGenerator, registry);
        this.modId = modId;
        this.existingFileHelper = existingFileHelper;
    }

    protected TagsProviderInject(DataGenerator dataGenerator, Registry<T> registry) {}

    @Nullable
    protected Path getPath(ResourceLocation id) {
        return this.pathProvider.json(id);
    }

    @Override
    public void kilt$setExistingFileHelper(ExistingFileHelper fileHelper) {
        this.existingFileHelper = fileHelper;
    }

    @Override
    public void kilt$setModId(String modId) {
        this.modId = modId;
    }

    @Mixin(TagsProvider.TagAppender.class)
    public static class TagAppenderInject<T> implements IForgeTagAppender<T>, TagAppenderInjection {
        @Shadow @Final private TagBuilder builder;
        @Unique
        private String modId;

        @CreateInitializer
        TagAppenderInject(TagBuilder builder, Registry<T> registry, String modId) {
            this(builder, registry);
            this.modId = modId;
        }

        TagAppenderInject(TagBuilder builder, Registry<T> registry) {}

        public TagsProvider.TagAppender<T> add(TagEntry tag) {
            this.builder.add(tag);
            return (TagsProvider.TagAppender<T>) (Object) this;
        }

        public TagBuilder getInternalBuilder() {
            return this.builder;
        }

        public String getModID() {
            return this.modId;
        }
    }
}
