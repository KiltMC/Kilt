package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.SeagrassBlock;
import net.neoforged.neoforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SeagrassBlock.class)
public abstract class SeagrassBlockInject implements IForgeShearable {
}
