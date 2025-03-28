package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Tier.class)
public interface TierInject {
    @Nullable
    default TagKey<Block> getTag() {
        return null;
    }
}
