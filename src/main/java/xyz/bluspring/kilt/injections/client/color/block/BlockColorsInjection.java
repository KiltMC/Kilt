package xyz.bluspring.kilt.injections.client.color.block;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public interface BlockColorsInjection {
    Map<Block, BlockColor> kilt$getBlockColors();
}
