package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraftforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockInject implements IForgeShearable {
}
