package xyz.bluspring.kilt.forgeinjects.tags;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.tags.TagFileInjection;

import java.util.List;

@Mixin(TagFile.class)
public abstract class TagFileInject implements TagFileInjection {
    @ModifyReturnValue(method = "method_43950", at = @At("RETURN"))
    private static App<RecordCodecBuilder.Mu<TagFile>, TagFile> kilt$appendRemoveTags(App<RecordCodecBuilder.Mu<TagFile>, TagFile> original, @Local(argsOnly = true) RecordCodecBuilder.Instance<TagFile> instance) {
        return instance.group(original,
            TagEntry.CODEC.listOf()
                .optionalFieldOf("remove", List.of())
                .forGetter(file -> ((TagFileInjection) (Object) file).remove())
        )
            .apply(instance, (file, remove) -> {
                ((TagFileInjection) (Object) file).kilt$setRemove(remove);
                return file;
            });
    }

    @Unique private List<TagEntry> remove = List.of();

    public List<TagEntry> remove() {
        return remove;
    }

    public TagFileInject(List<TagEntry> entries, boolean replace) {}

    @CreateInitializer
    public TagFileInject(List<TagEntry> entries, boolean replace, List<TagEntry> remove) {
        this(entries, replace);
        this.remove = remove;
    }

    @Override
    public void kilt$setRemove(List<TagEntry> remove) {
        this.remove = remove;
    }
}
