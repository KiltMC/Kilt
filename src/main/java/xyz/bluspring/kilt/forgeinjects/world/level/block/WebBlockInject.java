package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.WebBlock;
import net.minecraftforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WebBlock.class)
public abstract class WebBlockInject implements IForgeShearable {
}
