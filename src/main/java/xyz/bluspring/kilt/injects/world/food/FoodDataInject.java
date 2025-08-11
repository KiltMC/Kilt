// TRACKED HASH: 67468ffd11d7ad051ef0ba08d2f696e15a0b8fef
package xyz.bluspring.kilt.injects.world.food;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.world.food.FoodDataInjection;

import java.util.concurrent.atomic.AtomicReference;

@Mixin(FoodData.class)
public abstract class FoodDataInject implements FoodDataInjection {
    @Shadow public abstract void eat(Item item, ItemStack stack);

    @Unique private final AtomicReference<LivingEntity> kilt$entity = new AtomicReference<>(null);

    @Override
    public void eat(Item item, ItemStack stack, @Nullable LivingEntity entity) {
        this.kilt$entity.set(entity);
        this.eat(item, stack);
        this.kilt$entity.set(null);
    }

    @WrapOperation(method = "eat(Lnet/minecraft/world/item/Item;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties kilt$useEntityFoodPropertiesIfPossible(Item instance, Operation<FoodProperties> original, @Local(argsOnly = true) ItemStack stack) {
        var entity = this.kilt$entity.getAndSet(null);
        if (entity != null) {
            return stack.getFoodProperties(entity);
        }

        return original.call(instance);
    }
}