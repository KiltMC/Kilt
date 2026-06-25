package xyz.bluspring.kilt.injections.tags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public interface BlockTagsInjection {
    static TagKey<Block> create(Identifier name) {
        return TagKey.create(Registries.BLOCK, name);
    }
}
