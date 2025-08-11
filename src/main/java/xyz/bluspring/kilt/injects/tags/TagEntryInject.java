package xyz.bluspring.kilt.injects.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.tags.TagEntryInjection;

@Mixin(TagEntry.class)
public abstract class TagEntryInject implements TagEntryInjection {
    @Shadow @Final private ResourceLocation id;
    @Shadow @Final private boolean required;
    @Shadow @Final private boolean tag;

    @Override
    public ResourceLocation getId() {
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
