// TRACKED HASH: f83737c1722cf4974da22578c25c45ca640818c9
package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.level.ForcedChunksSavedData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.ForcedChunksSavedDataInjection;

@Mixin(ForcedChunksSavedData.class)
public class ForcedChunksSavedDataInject implements ForcedChunksSavedDataInjection {
}