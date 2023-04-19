package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.BucketPickup;
import net.minecraftforge.common.extensions.IForgeBucketPickup;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BucketPickup.class)
public interface BucketPickupInject extends IForgeBucketPickup {
}
