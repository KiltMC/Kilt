package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Cat.class)
public abstract class CatInject extends TamableAnimal {
    protected CatInject(EntityType<? extends TamableAnimal> entityType, Level level) {
        super(entityType, level);
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "get", method = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;")
    @Definition(id = "FOOD", field = "Lnet/minecraft/core/component/DataComponents;FOOD:Lnet/minecraft/core/component/DataComponentType;")
    @Expression("itemStack.get(FOOD)")
    @WrapOperation(method = "mobInteract", at = @At("MIXINEXTRAS:EXPRESSION"))
    private Object kilt$tryFeedFromStack(ItemStack instance, DataComponentType dataComponentType, Operation<Object> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "getFoodProperties", ItemStack.class, LivingEntity.class)) {
            return instance.getFoodProperties(this);
        }

        return original.call(instance, dataComponentType);
    }

    @Definition(id = "random", field = "Lnet/minecraft/world/entity/animal/Cat;random:Lnet/minecraft/util/RandomSource;")
    @Definition(id = "nextInt", method = "Lnet/minecraft/util/RandomSource;nextInt(I)I")
    @Expression("this.random.nextInt(?) == ?")
    @ModifyExpressionValue(method = "tryToTame", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkAnimalTameEvent(boolean original, @Local(argsOnly = true) Player player) {
        return original && !EventHooks.onAnimalTame(this, player);
    }
}
