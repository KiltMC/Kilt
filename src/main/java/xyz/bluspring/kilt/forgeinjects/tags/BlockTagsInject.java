package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.tags.BlockTags;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.tags.BlockTagsInjection;

@Mixin(BlockTags.class)
public class BlockTagsInject implements BlockTagsInjection {
}
