// TRACKED HASH: 91406160d3c0d0a33b7e173e57d20f7690d0528f
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.BucketPickup;
import net.minecraftforge.common.extensions.IForgeBucketPickup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BucketPickup.class)
public interface BucketPickupInject extends IForgeBucketPickup {
}