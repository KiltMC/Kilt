package xyz.bluspring.kilt.forgeinjects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Cat.class)
public abstract class CatInject extends TamableAnimal {
    protected CatInject(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getFoodProperties()Lnet/minecraft/world/food/FoodProperties;"))
    private FoodProperties kilt$tryFeedFromStack(Item instance, Operation<FoodProperties> original, @Local ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getFoodProperties", ItemStack.class, LivingEntity.class)) {
            return instance.getFoodProperties(stack, this);
        }

        return original.call(instance);
    }

    @Definition(id = "random", field = "Lnet/minecraft/world/entity/animal/Cat;random:Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("this.random.nextInt(?) == ?")
    @ModifyExpressionValue(method = "mobInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkAnimalTameEvent(boolean original, @Local(argsOnly = true) Player player) {
        return original && !ForgeEventFactory.onAnimalTame(this, player);
    }
}
