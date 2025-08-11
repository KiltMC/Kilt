package xyz.bluspring.kilt.injects.world.level.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RedStoneOreBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RedStoneOreBlock.class)
public abstract class RedStoneOreBlockInject extends Block {
    public RedStoneOreBlockInject(Properties properties) {
        super(properties);
    }

    // Kilt: handled by Porting Lib
}
