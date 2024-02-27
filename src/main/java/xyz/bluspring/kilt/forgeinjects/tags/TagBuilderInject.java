// TRACKED HASH: 0c86e781bc3655f83ffbb2cfb5802edb5d644707
package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagEntry;
import net.minecraftforge.common.extensions.IForgeRawTagBuilder;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.tags.TagBuilderInjection;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Mixin(TagBuilder.class)
public class TagBuilderInject implements TagBuilderInjection, IForgeRawTagBuilder {
    private final List<TagEntry> removeEntries = new ArrayList<>();

    @Override
    public Stream<TagEntry> getRemoveEntries() {
        return removeEntries.stream();
    }

    @Override
    public TagBuilder remove(TagEntry entry) {
        removeEntries.add(entry);
        return (TagBuilder) (Object) this;
    }

    // what the fuck is this even used for??
    private boolean replace = false;

    @Override
    public TagBuilder replace(boolean value) {
        this.replace = value;
        return (TagBuilder) (Object) this;
    }
}