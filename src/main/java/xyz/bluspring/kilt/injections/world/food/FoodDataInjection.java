package xyz.bluspring.kilt.injections.world.food;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface FoodDataInjection {
    default void eat(Item item, ItemStack stack, @Nullable LivingEntity entity)  {
        throw new IllegalStateException();
    }
}
