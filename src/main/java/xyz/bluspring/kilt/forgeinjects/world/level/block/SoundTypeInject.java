package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraftforge.common.data.SoundDefinition;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SoundDefinition.SoundType.class)
public abstract class SoundTypeInject {
    // Kilt: nothing here
}
