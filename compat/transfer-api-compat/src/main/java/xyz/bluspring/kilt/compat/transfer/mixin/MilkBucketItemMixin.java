package xyz.bluspring.kilt.compat.transfer.mixin;

import net.fabricmc.fabric.mixin.transfer.BucketItemAccessor;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemMixin implements BucketItemAccessor {
    @Override
    public Fluid fabric_getFluid() {
        return NeoForgeMod.MILK.asOptional().orElse(null);
    }
}
