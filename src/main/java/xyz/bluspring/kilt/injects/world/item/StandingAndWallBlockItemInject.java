package xyz.bluspring.kilt.injects.world.item;

import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;

@Mixin(StandingAndWallBlockItem.class)
public abstract class StandingAndWallBlockItemInject extends BlockItem {
    @Shadow @Final protected Block wallBlock;

    public StandingAndWallBlockItemInject(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        super.removeFromBlockToItemMap(blockToItemMap, itemIn);
        blockToItemMap.remove(this.wallBlock);
    }
}
