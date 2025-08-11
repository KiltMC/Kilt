package xyz.bluspring.kilt.injects.world.item;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.BlockItemExtensions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(StandingAndWallBlockItem.class)
public abstract class StandingAndWallBlockItemInject implements BlockItemExtensions {
    @Shadow @Final protected Block wallBlock;

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        BlockItemExtensions.super.removeFromBlockToItemMap(blockToItemMap, itemIn);
        blockToItemMap.remove(this.wallBlock);
    }
}
