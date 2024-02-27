// TRACKED HASH: debbd63809934e4b473f32625f60d1448d5c238d
package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.block.entity.HopperBlockEntityInjection;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityInject implements HopperBlockEntityInjection {
    @Shadow private long tickedGameTime;

    @Override
    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }
}