package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.LeavesBlock;
import net.neoforged.neoforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockInject implements IForgeShearable {
}
