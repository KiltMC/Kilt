// TRACKED HASH: f83737c1722cf4974da22578c25c45ca640818c9
package xyz.bluspring.kilt.forgeinjects.world.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.neoforged.neoforge.common.world.chunk.ForcedChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.ForcedChunksSavedDataInjection;

@Mixin(ForcedChunksSavedData.class)
public abstract class ForcedChunksSavedDataInject implements ForcedChunksSavedDataInjection {
    // Implemented by Porting Lib
}