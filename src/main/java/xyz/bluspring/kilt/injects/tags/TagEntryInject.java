package xyz.bluspring.kilt.injects.tags;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.tags.TagEntryInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagEntry;

@Mixin(TagEntry.class)
public abstract class TagEntryInject implements TagEntryInjection {
    @Shadow @Final private Identifier id;
    @Shadow @Final private boolean required;
    @Shadow @Final private boolean tag;

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public boolean isTag() {
        return tag;
    }
}
