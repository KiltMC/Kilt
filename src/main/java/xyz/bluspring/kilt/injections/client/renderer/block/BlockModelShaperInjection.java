package xyz.bluspring.kilt.injections.client.renderer.block;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockModelShaperInjection {
    TextureAtlasSprite getTexture(BlockState state, Level level, BlockPos pos);
}
