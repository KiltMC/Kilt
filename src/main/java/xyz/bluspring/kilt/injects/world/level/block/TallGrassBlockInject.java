package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.TallGrassBlock;
import net.neoforged.neoforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TallGrassBlock.class)
public abstract class TallGrassBlockInject implements IForgeShearable {
}
