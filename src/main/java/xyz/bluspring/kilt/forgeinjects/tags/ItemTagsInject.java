package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.tags.ItemTags;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.tags.ItemTagsInjection;

@Mixin(ItemTags.class)
public class ItemTagsInject implements ItemTagsInjection {
}
