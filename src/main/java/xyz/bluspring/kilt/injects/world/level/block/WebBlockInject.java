package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.WebBlock;
import net.neoforged.neoforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WebBlock.class)
public abstract class WebBlockInject implements IForgeShearable {
}
