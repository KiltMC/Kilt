// TRACKED HASH: 834fc3d6b652f61039af4bb7ec29cdea5fe3bc0e
package xyz.bluspring.kilt.injects.tags;

import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.BlockTagsInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

@Mixin(BlockTags.class)
public class BlockTagsInject implements BlockTagsInjection {
    @CreateStatic
    private static TagKey<Block> create(Identifier name) {
        return BlockTagsInjection.create(name);
    }
}
