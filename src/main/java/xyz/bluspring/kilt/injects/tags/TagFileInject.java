package xyz.bluspring.kilt.injects.tags;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.tags.TagFileInjection;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import net.fabricmc.fabric.api.tag.v1.FabricTagFile;
import net.fabricmc.fabric.impl.tag.TagFileHooks;

@Mixin(TagFile.class)
public abstract class TagFileInject implements TagFileInjection, FabricTagFile, TagFileHooks {
    @ModifyReturnValue(method = "method_43950", at = @At("RETURN"))
    private static App<RecordCodecBuilder.Mu<TagFile>, TagFile> kilt$appendRemoveTags(App<RecordCodecBuilder.Mu<TagFile>, TagFile> original, @Local(argsOnly = true) RecordCodecBuilder.Instance<TagFile> instance) {
        return instance.group(original,
            TagEntry.CODEC.listOf()
                .optionalFieldOf("remove", List.of())
                .forGetter(TagFileInjection::remove)
        )
            .apply(instance, (file, remove) -> {
                file.kilt$setRemove(remove);
                return file;
            });
    }

    // implemented by Fabric API
//    @Unique private List<TagEntry> remove = List.of();
//
//    public List<TagEntry> remove() {
//        return remove;
//    }

    public TagFileInject(List<TagEntry> entries, boolean replace) {}

    @CreateInitializer
    public TagFileInject(List<TagEntry> entries, boolean replace, List<TagEntry> remove) {
        this(entries, replace);
        this.fabric_setRemove(remove);
    }

    @Override
    public void kilt$setRemove(List<TagEntry> remove) {
        this.fabric_setRemove(remove);
    }
}
