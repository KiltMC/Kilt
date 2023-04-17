package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.BlockTagsInjection;

@Mixin(BlockTags.class)
public class BlockTagsInject implements BlockTagsInjection {
    @CreateStatic
    private static TagKey<Block> create(ResourceLocation name) {
        return BlockTagsInjection.create(name);
    }
}
