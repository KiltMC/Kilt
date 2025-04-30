package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockItemInjection {
    SoundEvent getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity);
}
