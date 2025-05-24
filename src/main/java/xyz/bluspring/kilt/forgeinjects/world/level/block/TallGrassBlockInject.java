package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraftforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TallGrassBlock.class)
public abstract class TallGrassBlockInject implements IForgeShearable {
}
