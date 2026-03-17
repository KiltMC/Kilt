package xyz.bluspring.kilt.injections.world.item;

import java.util.Map;

import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockItemInjection {
    default SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        throw KiltHelper.createMixinException(BlockItemInjection.class, "getPlaceSound");
    }

    default void removeFromBlockToItemMap(Map<Block, Item> blockToItemMap, Item itemIn) {
        throw KiltHelper.createMixinException(BlockItemInjection.class, "removeFromBlockToItemMap");
    }
}
