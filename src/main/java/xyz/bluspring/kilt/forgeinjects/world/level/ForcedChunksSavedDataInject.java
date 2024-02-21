package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.level.ForcedChunksSavedData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.ForcedChunksSavedDataInjection;

@Mixin(ForcedChunksSavedData.class)
public class ForcedChunksSavedDataInject implements ForcedChunksSavedDataInjection {
}
