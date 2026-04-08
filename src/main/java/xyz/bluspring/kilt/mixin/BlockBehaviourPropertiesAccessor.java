package xyz.bluspring.kilt.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootTable;

@Mixin(BlockBehaviour.Properties.class)
public interface BlockBehaviourPropertiesAccessor {
    @Accessor
    ResourceKey<LootTable> getDrops();
}
