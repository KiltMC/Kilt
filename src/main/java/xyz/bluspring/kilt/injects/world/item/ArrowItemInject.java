package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.item.ArrowItemInjection;

@Mixin(ArrowItem.class)
public abstract class ArrowItemInject implements ArrowItemInjection {
    @Override
    public boolean isInfinite(ItemStack ammo, ItemStack bow, LivingEntity livingEntity) {
        return false;
    }
}
