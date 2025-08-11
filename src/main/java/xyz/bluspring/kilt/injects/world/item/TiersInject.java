package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Tiers.class)
public abstract class TiersInject {
    @Nullable
    public TagKey<Block> getTag() {
        return CommonHooks.getTagFromVanillaTier((Tiers) (Object) this);
    }
}
